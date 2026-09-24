package co.panha.hibernate.tourmanagement.features.schedule;

import co.panha.hibernate.tourmanagement.features.guide.Guide;
import co.panha.hibernate.tourmanagement.features.guide.GuideRepository;
import co.panha.hibernate.tourmanagement.features.guide.GuideStatus;
import co.panha.hibernate.tourmanagement.features.schedule.dto.AssignGuideRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.CancelScheduleRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.CreateScheduleRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.ScheduleResponse;
import co.panha.hibernate.tourmanagement.features.schedule.dto.UpdateScheduleRequest;
import co.panha.hibernate.tourmanagement.features.tour.Tour;
import co.panha.hibernate.tourmanagement.features.tour.TourRepository;
import co.panha.hibernate.tourmanagement.utils.DateUtils;
import co.panha.hibernate.tourmanagement.utils.GenerateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {

    private static final String CODE_PREFIX = "SC";

    /** ស្ថានភាពដែលលែងកែបាន — ដំណើរបានចេញ បញ្ចប់ ឬបោះបង់រួច។ */
    private static final Set<ScheduleStatus> LOCKED_STATUSES =
            EnumSet.of(ScheduleStatus.DEPARTED, ScheduleStatus.COMPLETED, ScheduleStatus.CANCELLED);

    private final ScheduleRepository scheduleRepository;
    private final TourRepository tourRepository;
    private final GuideRepository guideRepository;
    private final ScheduleMapper scheduleMapper;

    @Override
    @Transactional
    public ScheduleResponse createNew(CreateScheduleRequest request) {

        // ១. Validate កាលបរិច្ឆេទ
        requireDateOrder(request.departureDate(), request.returnDate());

        // ２. Load
        Tour tour = tourRepository.findByIdAndIsDeletedFalse(request.tourId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tour not found with id = " + request.tourId()));

        // ៣. Check Rules
        requireDurationMatchesTour(request.departureDate(), request.returnDate(), tour);
        requireCapacityWithinTourLimits(request.capacity(), tour);

        Guide guide = null;
        if (request.guideId() != null) {
            guide = loadActiveGuide(request.guideId());
            requireGuideFree(guide, request.departureDate(), request.returnDate(), null);
        }

        // ５. Build
        TourSchedule schedule = new TourSchedule();
        schedule.setCode(nextScheduleCode(request.departureDate()));
        schedule.setTour(tour);
        schedule.setGuide(guide);
        schedule.setDepartureDate(request.departureDate());
        schedule.setReturnDate(request.returnDate());
        schedule.setDepartureTime(request.departureTime());
        schedule.setMeetingPoint(request.meetingPoint());
        schedule.setCapacity(request.capacity());
        schedule.setPriceOverride(request.priceOverride());
        schedule.setStatus(ScheduleStatus.OPEN);
        schedule.setIsDeleted(false);

        return toResponseWithSeats(scheduleRepository.save(schedule));
    }

    @Override
    public List<ScheduleResponse> findByTour(Long tourId, LocalDate fromDate) {

        Tour tour = tourRepository.findByIdAndIsDeletedFalse(tourId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tour not found with id = " + tourId));

        LocalDate from = (fromDate != null) ? fromDate : LocalDate.now();

        return scheduleRepository.findOpenSchedulesByTour(tour.getId(), from).stream()
                .map(this::toResponseWithSeats)
                .toList();
    }

    @Override
    public ScheduleResponse findById(Long id) {
        return toResponseWithSeats(loadById(id));
    }

    @Override
    @Transactional
    public ScheduleResponse updateById(Long id, UpdateScheduleRequest request) {

        TourSchedule schedule = loadById(id);
        requireEditable(schedule);

        LocalDate finalDeparture = (request.departureDate() != null)
                ? request.departureDate() : schedule.getDepartureDate();
        LocalDate finalReturn = (request.returnDate() != null)
                ? request.returnDate() : schedule.getReturnDate();

        boolean datesChanged = !finalDeparture.equals(schedule.getDepartureDate())
                || !finalReturn.equals(schedule.getReturnDate());

        if (datesChanged) {
            requireDateOrder(finalDeparture, finalReturn);
            requireDurationMatchesTour(finalDeparture, finalReturn, schedule.getTour());

            // មគ្គុទ្ទេសក៍ដែលចាត់តាំងរួចអាចជាប់រវល់ក្នុងចន្លោះថ្ងៃថ្មី
            if (schedule.getGuide() != null) {
                requireGuideFree(schedule.getGuide(), finalDeparture, finalReturn, schedule.getId());
            }

            schedule.setDepartureDate(finalDeparture);
            schedule.setReturnDate(finalReturn);
        }

        if (request.capacity() != null) {
            requireCapacityWithinTourLimits(request.capacity(), schedule.getTour());
            // TODO ដំណាក់កាល ４ (F7 Booking)៖ capacity ថ្មីមិនអាចតិចជាងកៅអីដែលកក់រួច
            //   int booked = bookingRepository.countOccupiedSeats(schedule.getId());
            //   if (request.capacity() < booked) {
            //       throw new ResponseStatusException(HttpStatus.CONFLICT,
            //               "capacity cannot be less than the seats already booked (" + booked + ")");
            //   }
            schedule.setCapacity(request.capacity());
        }

        if (request.departureTime() != null) {
            schedule.setDepartureTime(request.departureTime());
        }
        if (request.meetingPoint() != null) {
            schedule.setMeetingPoint(request.meetingPoint());
        }
        if (request.priceOverride() != null) {
            schedule.setPriceOverride(request.priceOverride());
        }

        return toResponseWithSeats(scheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public ScheduleResponse assignGuide(Long id, AssignGuideRequest request) {

        TourSchedule schedule = loadById(id);

        if (LOCKED_STATUSES.contains(schedule.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot reassign guide on a schedule in status " + schedule.getStatus());
        }

        Guide guide = loadActiveGuide(request.guideId());
        requireGuideFree(guide, schedule.getDepartureDate(), schedule.getReturnDate(), schedule.getId());

        schedule.setGuide(guide);

        return toResponseWithSeats(scheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public ScheduleResponse cancel(Long id, CancelScheduleRequest request) {

        TourSchedule schedule = loadById(id);

        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Schedule is already cancelled");
        }

        if (schedule.getStatus() == ScheduleStatus.DEPARTED || schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel a schedule that has already departed or completed");
        }

        schedule.setStatus(ScheduleStatus.CANCELLED);
        schedule.setCancelReason(request.reason());
        scheduleRepository.save(schedule);

        // TODO ដំណាក់កាល ４ (F7 Booking + F8 Payment)៖ លុបចោលការកក់ទាំងអស់ + សងប្រាក់វិញ
        //   for (Booking booking : bookingRepository.findActiveBySchedule(schedule.getId())) {
        //       booking.setStatus(BookingStatus.CANCELLED);
        //       booking.setCancelledAt(LocalDateTime.now());
        //       booking.setCancelReason("Schedule cancelled: " + request.reason());
        //       bookingRepository.save(booking);
        //       paymentService.refundFull(booking, "Schedule cancelled");
        //       notificationService.notifyScheduleCancelled(booking);
        //   }

        return toResponseWithSeats(schedule);
    }

    /**
     * រត់រៀងរាល់ថ្ងៃម៉ោង ០១:០០។
     *
     * <p>ប្រើ {@code isBefore(today)} មិនមែន {@code isBefore(tomorrow)} ទេ — កាលវិភាគដែលចេញ
     * <b>ថ្ងៃនេះ</b> នៅមិនទាន់ {@code DEPARTED} ព្រោះម៉ោង ០១:០០ ភ្ញៀវនៅមិនទាន់ចេញដំណើរ។
     * វាប្តូរនៅម៉ោង ០១:០០ ថ្ងៃបន្ទាប់វិញ។
     */
    @Override
    @Transactional
    @Scheduled(cron = "${app.jobs.refresh-schedule-statuses.cron:0 0 1 * * *}")
    public void refreshScheduleStatuses() {

        LocalDate today = LocalDate.now();

        // ក. ថ្ងៃចេញកន្លងផុត → DEPARTED
        List<TourSchedule> departing = scheduleRepository
                .findByStatusInAndDepartureDateBeforeAndIsDeletedFalse(
                        List.of(ScheduleStatus.OPEN, ScheduleStatus.FULL), today);

        departing.forEach(schedule -> schedule.setStatus(ScheduleStatus.DEPARTED));
        scheduleRepository.saveAll(departing);

        // ខ. ថ្ងៃត្រឡប់កន្លងផុត → COMPLETED
        List<TourSchedule> completing = scheduleRepository
                .findByStatusAndReturnDateBeforeAndIsDeletedFalse(ScheduleStatus.DEPARTED, today);

        completing.forEach(schedule -> schedule.setStatus(ScheduleStatus.COMPLETED));
        scheduleRepository.saveAll(completing);

        // TODO ដំណាក់កាល ４ (F7 Booking)៖ ការកក់ CONFIRMED → COMPLETED + អញ្ជើញវាយតម្លៃ
        //   for (TourSchedule schedule : completing) {
        //       for (Booking booking : bookingRepository.findByScheduleAndStatus(schedule.getId(), CONFIRMED)) {
        //           booking.setStatus(BookingStatus.COMPLETED);
        //           bookingRepository.save(booking);
        //           notificationService.inviteToReview(booking);
        //       }
        //   }

        log.info("refreshScheduleStatuses: DEPARTED {} · COMPLETED {}", departing.size(), completing.size());
    }

    // ---------- ជំនួយខាងក្នុង ----------

    private TourSchedule loadById(Long id) {
        return scheduleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Schedule not found with id = " + id));
    }

    private Guide loadActiveGuide(Long guideId) {
        Guide guide = guideRepository.findByIdAndIsDeletedFalse(guideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Guide not found with id = " + guideId));

        if (guide.getStatus() != GuideStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Guide is not active (" + guide.getStatus() + ")");
        }
        return guide;
    }

    private void requireDateOrder(LocalDate departureDate, LocalDate returnDate) {
        if (returnDate.isBefore(departureDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "returnDate must be on or after departureDate");
        }

        if (DateUtils.isPast(departureDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "departureDate cannot be in the past");
        }
    }

    /** រយៈពេលកាលវិភាគត្រូវត្រូវគ្នានឹង {@code tour.durationDays} — រាប់បញ្ចូលថ្ងៃចេញ។ */
    private void requireDurationMatchesTour(LocalDate departureDate, LocalDate returnDate, Tour tour) {
        long actualDays = DateUtils.daysBetween(departureDate, returnDate) + 1;

        if (actualDays != tour.getDurationDays()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Duration mismatch — tour lasts " + tour.getDurationDays()
                            + " day(s) but the schedule spans " + actualDays);
        }
    }

    private void requireCapacityWithinTourLimits(Integer capacity, Tour tour) {
        if (capacity > tour.getMaxGroupSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "capacity cannot exceed the tour maxGroupSize (" + tour.getMaxGroupSize() + ")");
        }

        if (tour.getMinGroupSize() != null && capacity < tour.getMinGroupSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "capacity cannot be less than the tour minGroupSize (" + tour.getMinGroupSize() + ")");
        }
    }

    private void requireGuideFree(Guide guide, LocalDate startDate, LocalDate endDate, Long excludeScheduleId) {
        if (scheduleRepository.hasGuideConflict(guide.getId(), startDate, endDate, excludeScheduleId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Guide already has an overlapping schedule in this date range");
        }
    }

    private void requireEditable(TourSchedule schedule) {
        if (LOCKED_STATUSES.contains(schedule.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot edit a schedule in status " + schedule.getStatus());
        }
    }

    /** បំពេញកៅអី និងតម្លៃពិត — ទិន្នន័យដែលគណនាពេលអាន មិនរក្សាទុក។ */
    private ScheduleResponse toResponseWithSeats(TourSchedule schedule) {

        // TODO ដំណាក់កាល ４ (F7 Booking)៖ បូកកៅអីដែលកក់រួច
        //   int booked = bookingRepository.countOccupiedSeats(schedule.getId());
        int booked = 0;

        int available = schedule.getCapacity() - booked;

        return scheduleMapper.toResponse(schedule, booked, available, schedule.effectivePrice());
    }

    /** លេខកូដតាមថ្ងៃចេញ + លេខចៃដន្យ — រំកិលរហូតដល់លេខទំនេរ។ */
    private String nextScheduleCode(LocalDate departureDate) {
        String code = GenerateUtils.generateDateCode(CODE_PREFIX, departureDate);

        int attempts = 0;
        while (scheduleRepository.existsByCode(code) && attempts < 100) {
            code = GenerateUtils.generateDateCode(CODE_PREFIX, departureDate);
            attempts++;
        }
        return code;
    }
}
