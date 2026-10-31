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

    // BR8: cancellation allowed only at least 3 days before departure.
    private static final int MIN_DAYS_BEFORE_CANCEL = 3;

    // BR9: at least 50% must be paid before confirmation.
    private static final BigDecimal MIN_PAID_RATE_TO_CONFIRM = new BigDecimal("0.50");

    // How long a PENDING booking waits for payment before auto-cancel.
    private static final int PENDING_EXPIRY_HOURS = 24;

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingMapper bookingMapper;
    private final PaymentService paymentService;

    // UC7.1: locks the schedule row (findByIdForUpdate) before counting seats to prevent overselling;
    // @Version cannot help here because two new rows are inserted, not the same row updated.
    @Override
    @Transactional
    public BookingDetailResponse bookTour(CreateBookingRequest request) {

        // 1. Validate
        requirePassengerListMatches(request.numberOfPeople(), request.passengers());

        // 2. Load
        Customer customer = loadMe();

        TourSchedule schedule = scheduleRepository.findByIdForUpdate(request.scheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Schedule not found with id = " + request.scheduleId()));

        Tour tour = schedule.getTour();

        // 3. Check Rules
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

        // 4. Calculate
        BigDecimal unitPrice = schedule.effectivePrice();
        BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(request.numberOfPeople()));
        BigDecimal discount = calculateDiscount(request.numberOfPeople(), subTotal);

        // 5. Build
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

        // 6. Save
        Booking saved = bookingRepository.save(booking);

        // 7. Side Effects
        if (bookedSeats + request.numberOfPeople() >= schedule.getCapacity()) {
            schedule.setStatus(ScheduleStatus.FULL);
            scheduleRepository.save(schedule);
        }

        // TODO: notificationService.sendBookingCreated(saved)
        log.info("Booking {} created by customer {} for schedule {} ({} seat(s))",
                saved.getCode(), customer.getId(), schedule.getId(), saved.getNumberOfPeople());

        // 8. Return
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

        // customerName stays null: no point showing a customer their own name.
        return PageMapper.toPageResponse(result, booking -> bookingMapper.toResponse(booking, null));
    }

    @Override
    public BookingDetailResponse findByCode(String code) {
        boolean isAdmin = AuthUtils.isAdmin();
        return toDetailResponse(loadByCodeForCurrentUser(code, true), isAdmin);
    }

    // UC7.4: reopens FULL -> OPEN only when the departure date is in the future.
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

        // Refund by the BR8 rate; the remainder is the cancellation penalty.
        if (refundAmount.signum() > 0) {
            BigDecimal refunded = paymentService.refundForBooking(booking, refundAmount,
                    "Booking cancelled: " + request.reason());

            log.info("Booking {} cancelled {} day(s) before departure — refund {} of {} paid",
                    booking.getCode(), daysLeft, refunded, booking.getPaidAmount());
        }

        syncScheduleAvailability(booking.getSchedule());

        // TODO: notificationService.sendBookingCancelled(booking, refundAmount)

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

        // TODO: notificationService.sendBookingConfirmed(booking)

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

    // UC7.7: seat count excludes this booking itself, otherwise a 3 -> 3 edit would double-count its own seats.
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

        // Recalculate totals; unitPrice stays as the booking-time snapshot.
        BigDecimal subTotal = booking.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.numberOfPeople()));
        BigDecimal discount = calculateDiscount(request.numberOfPeople(), subTotal);

        booking.setNumberOfPeople(request.numberOfPeople());
        booking.setSubTotal(subTotal);
        booking.setDiscountAmount(discount);
        booking.setTotalPrice(subTotal.subtract(discount));

        // Fewer passengers may leave an overpayment, which must be refunded.
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

        // Seat count changed: the schedule may reopen or become full.
        syncScheduleAvailability(schedule);

        return toDetailResponse(saved, false);
    }

    // Cancels PENDING bookings unpaid after the expiry window; partially paid ones are left to ADMIN.
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

            // TODO: notificationService.sendBookingAutoCancelled(booking)
        }

        log.info("autoCancelExpiredPending: examined {} · cancelled {}", expired.size(), cancelled);
    }

    // ---------- Business rules ----------

    // BR10: group-size discount.
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

    // Refund rate based on days remaining before departure.
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

    // BR7: passenger list must match numberOfPeople and have exactly one lead.
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

    // BR6: party size must be between minGroupSize and maxGroupSize.
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

    // BR3: enough seats must remain.
    private void requireEnoughSeats(int requested, int availableSeats) {

        if (requested <= availableSeats) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, availableSeats <= 0
                ? "This schedule is fully booked"
                : "Only " + availableSeats + " seat(s) left");
    }

    // BR4: account must be ACTIVE.
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

    // ---------- Internal helpers ----------

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

    // Loads a booking and checks ownership; non-owners get 404 (not 403) to avoid leaking code existence.
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

    // Syncs OPEN <-> FULL with real seat usage; other statuses are untouched. Call after every seat-count change.
    private void syncScheduleAvailability(TourSchedule schedule) {

        ScheduleStatus current = schedule.getStatus();

        if (current != ScheduleStatus.OPEN && current != ScheduleStatus.FULL) {
            return;
        }

        int occupied = bookingRepository.countOccupiedSeats(schedule.getId());
        boolean isFull = occupied >= schedule.getCapacity();

        ScheduleStatus target = isFull ? ScheduleStatus.FULL : ScheduleStatus.OPEN;

        // Do not reopen a trip whose departure date has passed.
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

    // Next code (BK-20260912-0001); advances to a free number because deleted bookings skew a plain count.
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
