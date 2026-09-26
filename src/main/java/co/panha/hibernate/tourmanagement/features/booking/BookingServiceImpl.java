package co.panha.hibernate.tourmanagement.features.booking;

import co.panha.hibernate.tourmanagement.base.PageMapper;
import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingDetailResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.CancelBookingRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.CreateBookingRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.PassengerRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.UpdatePassengersRequest;
import co.panha.hibernate.tourmanagement.features.customer.Customer;
import co.panha.hibernate.tourmanagement.features.payment.PaymentService;
import co.panha.hibernate.tourmanagement.features.customer.CustomerRepository;
import co.panha.hibernate.tourmanagement.features.customer.CustomerStatus;
import co.panha.hibernate.tourmanagement.features.schedule.ScheduleRepository;
import co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus;
import co.panha.hibernate.tourmanagement.features.schedule.TourSchedule;
import co.panha.hibernate.tourmanagement.features.tour.Tour;
import co.panha.hibernate.tourmanagement.security.AuthUtils;
import co.panha.hibernate.tourmanagement.utils.DateUtils;
import co.panha.hibernate.tourmanagement.utils.GenerateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final String CODE_PREFIX = "BK";

    /** BR8 — លុបចោលបានតែមុនថ្ងៃចេញដំណើរយ៉ាងតិច ៣ ថ្ងៃ។ */
    private static final int MIN_DAYS_BEFORE_CANCEL = 3;

    /** BR9 — ត្រូវបង់យ៉ាងតិច ៥០% មុនបញ្ជាក់។ */
    private static final BigDecimal MIN_PAID_RATE_TO_CONFIRM = new BigDecimal("0.50");

    /** រយៈពេលដែលការកក់ PENDING រង់ចាំការទូទាត់មុនលុបចោលស្វ័យប្រវត្តិ។ */
    private static final int PENDING_EXPIRY_HOURS = 24;

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingMapper bookingMapper;
    private final PaymentService paymentService;

    /**
     * UC7.1 — កក់ Tour។
     *
     * <p><b>ការពារ race condition</b>៖ ចាក់សោជួរកាលវិភាគ ({@code findByIdForUpdate})
     * មុនរាប់កៅអី។ បើគ្មានសោ អតិថិជនពីរនាក់អាចអានថា «សល់ ២ កៅអី» ព្រមគ្នា ហើយកក់
     * ម្នាក់ ២ កៅអី — ទៅជាលក់លើសចំណុះ។
     *
     * <p>{@code @Version} លើ {@code Booking} មិនគ្រប់គ្រាន់សម្រាប់ករណីនេះទេ ព្រោះវាការពារ
     * ការកែ <b>ជួរដដែល</b> ប៉ុណ្ណោះ ចំណែកនេះជាការបញ្ចូលជួរ<b>ថ្មីពីរ</b>។
     */
    @Override
    @Transactional
    public BookingDetailResponse bookTour(CreateBookingRequest request) {

        // ១. Validate
        requirePassengerListMatches(request.numberOfPeople(), request.passengers());

        // ២. Load
        Customer customer = loadMe();

        TourSchedule schedule = scheduleRepository.findByIdForUpdate(request.scheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Schedule not found with id = " + request.scheduleId()));

        Tour tour = schedule.getTour();

        // ៣. Check Rules
        requireActiveCustomer(customer);

        if (schedule.getStatus() != ScheduleStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This schedule is not open for booking (" + schedule.getStatus() + ")");
        }

        if (!schedule.getDepartureDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This schedule is no longer accepting bookings");
        }

        if (bookingRepository.existsActiveBooking(customer.getId(), schedule.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already booked this schedule");
        }

        requireGroupSizeWithinTourLimits(request.numberOfPeople(), tour);

        int bookedSeats = bookingRepository.countOccupiedSeats(schedule.getId());
        requireEnoughSeats(request.numberOfPeople(), schedule.getCapacity() - bookedSeats);

        // ៤. Calculate
        BigDecimal unitPrice = schedule.effectivePrice();
        BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(request.numberOfPeople()));
        BigDecimal discount = calculateDiscount(request.numberOfPeople(), subTotal);

        // ៥. Build
        Booking booking = new Booking();
        booking.setCode(nextBookingCode());
        booking.setCustomer(customer);
        booking.setSchedule(schedule);
        booking.setNumberOfPeople(request.numberOfPeople());
        booking.setUnitPrice(unitPrice);
        booking.setSubTotal(subTotal);
        booking.setDiscountAmount(discount);
        booking.setTotalPrice(subTotal.subtract(discount));
        booking.setPaidAmount(BigDecimal.ZERO);
        booking.setStatus(BookingStatus.PENDING);
        booking.setNote(request.note());
        booking.setBookedAt(LocalDateTime.now());
        booking.setIsDeleted(false);

        request.passengers().forEach(passenger ->
                booking.addPassenger(bookingMapper.toPassenger(passenger)));

        // ៦. Save
        Booking saved = bookingRepository.save(booking);

        // ៧. Side Effects
        if (bookedSeats + request.numberOfPeople() >= schedule.getCapacity()) {
            schedule.setStatus(ScheduleStatus.FULL);
            scheduleRepository.save(schedule);
        }

        // TODO ដំណាក់កាល ៥៖ notificationService.sendBookingCreated(saved)
        log.info("Booking {} created by customer {} for schedule {} ({} seat(s))",
                saved.getCode(), customer.getId(), schedule.getId(), saved.getNumberOfPeople());

        // ៨. Return
        return toDetailResponse(saved, false);
    }

    @Override
    public PageResponse<BookingResponse> findMyBookings(BookingStatus status, Integer page, Integer size) {

        Customer customer = loadMe();
        Pageable pageable = PageMapper.buildPageable(page, size, "bookedAt", Sort.Direction.DESC);

        Page<Booking> result = (status == null)
                ? bookingRepository.findAllByCustomerIdAndIsDeletedFalse(customer.getId(), pageable)
                : bookingRepository.findAllByCustomerIdAndStatusAndIsDeletedFalse(
                        customer.getId(), status, pageable);

        // customerName ទុក null — គ្មានប្រយោជន៍បង្ហាញឈ្មោះខ្លួនឯងក្នុងបញ្ជីខ្លួនឯង
        return PageMapper.toPageResponse(result, booking -> bookingMapper.toResponse(booking, null));
    }

    @Override
    public BookingDetailResponse findByCode(String code) {
        boolean isAdmin = AuthUtils.isAdmin();
        return toDetailResponse(loadByCodeForCurrentUser(code, true), isAdmin);
    }

    /**
     * UC7.4 — លុបចោល។
     *
     * <p>បើកកៅអីវិញដោយប្តូរ {@code FULL} ត្រឡប់ទៅ {@code OPEN} — តែ<b>តែពេល</b>
     * ថ្ងៃចេញដំណើរនៅអនាគត។ កាលវិភាគដែល {@code DEPARTED} ឬ {@code COMPLETED}
     * មិនត្រូវបើកទទួលការកក់វិញទេ។
     */
    @Override
    @Transactional
    public BookingResponse cancel(String code, CancelBookingRequest request) {

        Booking booking = loadByCodeForCurrentUser(code, false);

        requireCancellable(booking);

        long daysLeft = DateUtils.daysBetween(LocalDate.now(), booking.getSchedule().getDepartureDate());

        if (daysLeft < MIN_DAYS_BEFORE_CANCEL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cancellation must be at least " + MIN_DAYS_BEFORE_CANCEL
                            + " days before departure (" + daysLeft + " day(s) left)");
        }

        BigDecimal refundAmount = booking.getPaidAmount()
                .multiply(calculateRefundRate(daysLeft))
                .setScale(2, RoundingMode.HALF_UP);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(request.reason());
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.saveAndFlush(booking);

        // សងវិញតាមអត្រា BR8 — ភាគដែលនៅសល់ជាការពិន័យលុបចោល
        if (refundAmount.signum() > 0) {
            BigDecimal refunded = paymentService.refundForBooking(booking, refundAmount,
                    "Booking cancelled: " + request.reason());

            log.info("Booking {} cancelled {} day(s) before departure — refund {} of {} paid",
                    booking.getCode(), daysLeft, refunded, booking.getPaidAmount());
        }

        syncScheduleAvailability(booking.getSchedule());

        // TODO ដំណាក់កាល ៥៖ notificationService.sendBookingCancelled(booking, refundAmount)

        return toResponse(booking, false);
    }

    @Override
    @Transactional
    public BookingResponse confirm(String code) {

        Booking booking = loadByCode(code);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only PENDING bookings can be confirmed (current: " + booking.getStatus() + ")");
        }

        BigDecimal minimumRequired = booking.getTotalPrice()
                .multiply(MIN_PAID_RATE_TO_CONFIRM)
                .setScale(2, RoundingMode.HALF_UP);

        if (booking.getPaidAmount().compareTo(minimumRequired) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "At least 50% (" + minimumRequired + ") must be paid before confirming — paid "
                            + booking.getPaidAmount());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // TODO ដំណាក់កាល ៥៖ notificationService.sendBookingConfirmed(booking)

        return toResponse(booking, true);
    }

    @Override
    public PageResponse<BookingResponse> findAll(BookingStatus status, LocalDate fromDate, LocalDate toDate,
                                                 Integer page, Integer size) {

        Pageable pageable = PageMapper.buildPageable(page, size, "bookedAt", Sort.Direction.DESC);

        Page<Booking> result = bookingRepository.search(status, fromDate, toDate, pageable);

        return PageMapper.toPageResponse(result,
                booking -> bookingMapper.toResponse(booking, booking.getCustomer().getFullName()));
    }

    /**
     * UC7.7 — កែចំនួន និងបញ្ជីអ្នកដំណើរ។
     *
     * <p>ការរាប់កៅអីត្រូវ<b>ដកការកក់បច្ចុប្បន្នចេញ</b> មុនប្រៀបធៀប បើមិនដូច្នេះទេ
     * ការកែពី ៣ នាក់ទៅ ៣ នាក់ដដែលក៏នឹងបរាជ័យដែរ ព្រោះកៅអីរបស់ខ្លួនឯងត្រូវរាប់ពីរដង។
     */
    @Override
    @Transactional
    public BookingDetailResponse updatePassengers(String code, UpdatePassengersRequest request) {

        requirePassengerListMatches(request.numberOfPeople(), request.passengers());

        Booking booking = loadByCodeForCurrentUser(code, false);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Passengers can be changed only while the booking is PENDING (current: "
                            + booking.getStatus() + ")");
        }

        TourSchedule schedule = booking.getSchedule();
        requireGroupSizeWithinTourLimits(request.numberOfPeople(), schedule.getTour());

        int occupiedByOthers = bookingRepository.countOccupiedSeats(schedule.getId())
                - booking.getNumberOfPeople();
        requireEnoughSeats(request.numberOfPeople(), schedule.getCapacity() - occupiedByOthers);

        // គណនាតម្លៃឡើងវិញ — unitPrice នៅដដែល (snapshot ពេលកក់)
        BigDecimal subTotal = booking.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.numberOfPeople()));
        BigDecimal discount = calculateDiscount(request.numberOfPeople(), subTotal);

        booking.setNumberOfPeople(request.numberOfPeople());
        booking.setSubTotal(subTotal);
        booking.setDiscountAmount(discount);
        booking.setTotalPrice(subTotal.subtract(discount));

        // កាត់បន្ថយអ្នកដំណើរអាចធ្វើឲ្យបង់លើស — ត្រូវសងផ្នែកលើសវិញ
        BigDecimal overpaid = booking.getPaidAmount().subtract(booking.getTotalPrice());
        if (overpaid.signum() > 0) {
            paymentService.refundForBooking(booking, overpaid, "Reduced number of passengers");
            log.info("Booking {} refunded {} after reducing to {} passenger(s)",
                    booking.getCode(), overpaid, request.numberOfPeople());
        }

        booking.clearPassengers();
        request.passengers().forEach(passenger ->
                booking.addPassenger(bookingMapper.toPassenger(passenger)));

        Booking saved = bookingRepository.saveAndFlush(booking);

        // ចំនួនកៅអីប្តូរ — កាលវិភាគអាចទំនេរវិញ ឬពេញឡើង
        syncScheduleAvailability(schedule);

        return toDetailResponse(saved, false);
    }

    /**
     * លុបចោលការកក់ {@code PENDING} ដែលមិនបានទូទាត់ក្នុង {@value #PENDING_EXPIRY_HOURS} ម៉ោង។
     *
     * <p>ការកក់ដែលបង់ប្រាក់ខ្លះរួច <b>មិនលុបទេ</b> — ទុកឲ្យ ADMIN សម្រេច ព្រោះការលុបវា
     * ដោយស្វ័យប្រវត្តិនឹងបង្កើតកាតព្វកិច្ចសងប្រាក់ដោយគ្មានមនុស្សដឹង។
     */
    @Override
    @Transactional
    @Scheduled(cron = "${app.jobs.auto-cancel-pending.cron:0 */30 * * * *}")
    public void autoCancelExpiredPending() {

        LocalDateTime cutoff = LocalDateTime.now().minusHours(PENDING_EXPIRY_HOURS);
        List<Booking> expired = bookingRepository.findExpiredPending(cutoff);

        int cancelled = 0;

        for (Booking booking : expired) {
            if (booking.getPaidAmount() != null && booking.getPaidAmount().signum() > 0) {
                continue;
            }

            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelReason("Automatically cancelled: not paid within "
                    + PENDING_EXPIRY_HOURS + " hours");
            booking.setCancelledAt(LocalDateTime.now());
            bookingRepository.save(booking);

            syncScheduleAvailability(booking.getSchedule());
            cancelled++;

            // TODO ដំណាក់កាល ៥៖ notificationService.sendBookingAutoCancelled(booking)
        }

        log.info("autoCancelExpiredPending: examined {} · cancelled {}", expired.size(), cancelled);
    }

    // ---------- វិន័យអាជីវកម្ម ----------

    /** BR10 — បញ្ចុះតម្លៃតាមទំហំក្រុម។ */
    private BigDecimal calculateDiscount(int numberOfPeople, BigDecimal subTotal) {

        BigDecimal rate = BigDecimal.ZERO;

        if (numberOfPeople >= 10) {
            rate = new BigDecimal("0.15");
        } else if (numberOfPeople >= 5) {
            rate = new BigDecimal("0.10");
        } else if (numberOfPeople >= 3) {
            rate = new BigDecimal("0.05");
        }

        return subTotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /** អត្រាសងប្រាក់វិញតាមចំនួនថ្ងៃដែលនៅសល់មុនចេញដំណើរ។ */
    private BigDecimal calculateRefundRate(long daysLeft) {
        if (daysLeft >= 30) {
            return BigDecimal.ONE;
        }
        if (daysLeft >= 14) {
            return new BigDecimal("0.75");
        }
        if (daysLeft >= 7) {
            return new BigDecimal("0.50");
        }
        if (daysLeft >= MIN_DAYS_BEFORE_CANCEL) {
            return new BigDecimal("0.25");
        }
        return BigDecimal.ZERO;
    }

    /** BR7 — បញ្ជីអ្នកដំណើរត្រូវមានចំនួនស្មើ numberOfPeople ហើយមានអ្នកមេតែម្នាក់។ */
    private void requirePassengerListMatches(Integer numberOfPeople, List<PassengerRequest> passengers) {

        if (passengers.size() != numberOfPeople) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Passenger list size (" + passengers.size()
                            + ") does not match numberOfPeople (" + numberOfPeople + ")");
        }

        long leadCount = passengers.stream().filter(p -> Boolean.TRUE.equals(p.isLead())).count();

        if (leadCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Exactly one passenger must be marked as lead (found " + leadCount + ")");
        }
    }

    /** BR6 — ចំនួនអ្នកដំណើរត្រូវនៅចន្លោះ minGroupSize និង maxGroupSize។ */
    private void requireGroupSizeWithinTourLimits(int numberOfPeople, Tour tour) {

        if (tour.getMinGroupSize() != null && numberOfPeople < tour.getMinGroupSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This tour requires at least " + tour.getMinGroupSize() + " people");
        }

        if (numberOfPeople > tour.getMaxGroupSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This tour accepts at most " + tour.getMaxGroupSize() + " people");
        }
    }

    /** BR3 — កៅអីនៅសល់ត្រូវគ្រប់គ្រាន់។ */
    private void requireEnoughSeats(int requested, int availableSeats) {

        if (requested <= availableSeats) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, availableSeats <= 0
                ? "This schedule is fully booked"
                : "Only " + availableSeats + " seat(s) left");
    }

    /** BR4 — គណនីត្រូវស្ថិតក្នុងស្ថានភាព ACTIVE។ */
    private void requireActiveCustomer(Customer customer) {
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account cannot make bookings (" + customer.getStatus() + ")");
        }
    }

    private void requireCancellable(Booking booking) {

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot cancel a booking for a tour that has already been completed");
        }
    }

    // ---------- ជំនួយខាងក្នុង ----------

    private Customer loadMe() {
        String keycloakId = AuthUtils.currentKeycloakId();

        return customerRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No customer profile is linked to your account"));
    }

    private Booking loadByCode(String code) {
        return bookingRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found with code = " + code));
    }

    /**
     * ទាញការកក់ ហើយពិនិត្យសិទ្ធិជាម្ចាស់។
     *
     * <p>បោះ <b>404</b> មិនមែន 403 ទេពេលមិនមែនម្ចាស់ — ដើម្បីកុំឲ្យអ្នកវាយប្រហារ
     * ដឹងថាលេខកូដការកក់នេះមានពិត (ការលេចព័ត៌មានមួយប្រភេទ)។
     *
     * @param allowAdmin ពិត បើ ADMIN អាចមើលការកក់របស់អ្នកដទៃបាន (UC7.3)
     */
    private Booking loadByCodeForCurrentUser(String code, boolean allowAdmin) {

        Booking booking = loadByCode(code);

        if (allowAdmin && AuthUtils.isAdmin()) {
            return booking;
        }

        if (!booking.getCustomer().getKeycloakId().equals(AuthUtils.currentKeycloakId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Booking not found with code = " + code);
        }

        return booking;
    }

    /**
     * កែស្ថានភាពកាលវិភាគឲ្យត្រូវនឹងចំនួនកៅអីពិត — {@code OPEN} ⇄ {@code FULL}។
     *
     * <p>ប្តូរតែរវាង ២ ស្ថានភាពនេះប៉ុណ្ណោះ។ កាលវិភាគដែល {@code CLOSED}, {@code DEPARTED},
     * {@code COMPLETED} ឬ {@code CANCELLED} មិនត្រូវប៉ះទេ — ការបើកវាទទួលការកក់វិញជាកំហុស។
     *
     * <p>ហៅវាបន្ទាប់ពី<b>រាល់</b>ការប្តូរចំនួនកៅអី (លុបចោល · កែអ្នកដំណើរ) ព្រោះការប្តូរ
     * អាចទៅទាំងពីរទិស — កាត់បន្ថយធ្វើឲ្យទំនេរ ចំណែកបង្កើនអាចធ្វើឲ្យពេញ។
     */
    private void syncScheduleAvailability(TourSchedule schedule) {

        ScheduleStatus current = schedule.getStatus();

        if (current != ScheduleStatus.OPEN && current != ScheduleStatus.FULL) {
            return;
        }

        int occupied = bookingRepository.countOccupiedSeats(schedule.getId());
        boolean isFull = occupied >= schedule.getCapacity();

        ScheduleStatus target = isFull ? ScheduleStatus.FULL : ScheduleStatus.OPEN;

        // កុំបើកទទួលការកក់វិញលើដំណើរដែលថ្ងៃចេញកន្លងផុតហើយ
        if (target == ScheduleStatus.OPEN && !schedule.getDepartureDate().isAfter(LocalDate.now())) {
            return;
        }

        if (current != target) {
            schedule.setStatus(target);
            scheduleRepository.save(schedule);
        }
    }

    private BookingResponse toResponse(Booking booking, boolean includeCustomerName) {
        return bookingMapper.toResponse(booking,
                includeCustomerName ? booking.getCustomer().getFullName() : null);
    }

    private BookingDetailResponse toDetailResponse(Booking booking, boolean includeCustomerName) {
        return bookingMapper.toDetailResponse(booking,
                includeCustomerName ? booking.getCustomer().getFullName() : null);
    }

    /**
     * លេខកូដបន្ទាប់ — {@code BK-20260912-0001}។
     *
     * <p>រំកិលទៅមុខរហូតដល់លេខទំនេរ ដូចលំនាំ {@code nextGuideCode} ដែរ —
     * ព្រោះការរាប់តែម្នាក់ឯងមិនគ្រប់គ្រាន់ពេលមានការកក់ដែលលុបចេញ។
     */
    private String nextBookingCode() {

        LocalDate today = LocalDate.now();
        long count = bookingRepository.countBookingsOn(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        String code = GenerateUtils.generateDateSequentialCode(CODE_PREFIX, today, count);
        while (bookingRepository.existsByCode(code)) {
            count++;
            code = GenerateUtils.generateDateSequentialCode(CODE_PREFIX, today, count);
        }
        return code;
    }
}
