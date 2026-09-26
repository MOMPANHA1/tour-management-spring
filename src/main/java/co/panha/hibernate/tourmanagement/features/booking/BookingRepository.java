package co.panha.hibernate.tourmanagement.features.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** {@code EntityGraph} ការពារ N+1 ព្រោះ response ត្រូវការ tour និង schedule ជានិច្ច។ */
    @EntityGraph(attributePaths = {"customer", "schedule", "schedule.tour", "schedule.guide"})
    Optional<Booking> findByCodeAndIsDeletedFalse(String code);

    boolean existsByCode(String code);

    long countByCustomerIdAndIsDeletedFalse(Long customerId);

    long countByCustomerIdAndStatusAndIsDeletedFalse(Long customerId, BookingStatus status);

    @EntityGraph(attributePaths = {"schedule", "schedule.tour"})
    Page<Booking> findAllByCustomerIdAndIsDeletedFalse(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"schedule", "schedule.tour"})
    Page<Booking> findAllByCustomerIdAndStatusAndIsDeletedFalse(
            Long customerId, BookingStatus status, Pageable pageable);

    /**
     * កៅអីដែលកាន់កាប់រួច — {@code PENDING} រាប់ផង ព្រោះការកក់ដែលមិនទាន់បង់ប្រាក់
     * នៅតែកាន់កៅអីទុក (រហូតដល់ job លុបចោលវាក្នុង ២៤ ម៉ោង)។
     *
     * <p>{@code COALESCE} ចាំបាច់ — {@code SUM} លើសំណុំទទេត្រឡប់ {@code null} មិនមែន 0 ទេ។
     */
    @Query("""
            SELECT COALESCE(SUM(b.numberOfPeople), 0) FROM Booking b
            WHERE b.schedule.id = :scheduleId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.COMPLETED)
            """)
    int countOccupiedSeats(@Param("scheduleId") Long scheduleId);

    /** BR5 — អតិថិជនម្នាក់កក់កាលវិភាគដដែលពីរដងមិនបាន។ */
    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.customer.id = :customerId
              AND b.schedule.id = :scheduleId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED)
            """)
    boolean existsActiveBooking(@Param("customerId") Long customerId,
                                @Param("scheduleId") Long scheduleId);

    /** ប្រើដោយ F4 Tour — ហាមលុប Tour ដែលនៅមានការកក់សកម្ម។ */
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.schedule.tour.id = :tourId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED)
            """)
    long countActiveBookingsByTour(@Param("tourId") Long tourId);

    /** ប្រើដោយ F6 Customer — ទាមទារហេតុផលពេលផ្អាកអតិថិជនដែលមានការកក់សកម្ម។ */
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.customer.id = :customerId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED)
            """)
    long countActiveByCustomer(@Param("customerId") Long customerId);

    /** ប្រើដោយ F5 Schedule — លុបចោលការកក់ទាំងអស់ពេលកាលវិភាគត្រូវបានបោះបង់។ */
    @EntityGraph(attributePaths = {"customer"})
    @Query("""
            SELECT b FROM Booking b
            WHERE b.schedule.id = :scheduleId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED)
            """)
    List<Booking> findActiveBySchedule(@Param("scheduleId") Long scheduleId);

    /** ប្រើដោយ F5 Schedule — ការកក់ CONFIRMED → COMPLETED ពេលដំណើរបញ្ចប់។ */
    List<Booking> findByScheduleIdAndStatusAndIsDeletedFalse(Long scheduleId, BookingStatus status);

    /**
     * ចំនួនការកក់ក្នុងថ្ងៃមួយ — សម្រាប់បង្កើតលេខកូដតាមលំដាប់។
     *
     * <p>ប្រៀបធៀបចន្លោះ {@code >= ថ្ងៃនេះ 00:00} និង {@code < ថ្ងៃស្អែក 00:00} ជំនួសឲ្យ
     * {@code DATE(b.bookedAt)} ព្រោះ function នោះមិនស្តង់ដារក្នុង JPQL ហើយបំបែក index។
     */
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.bookedAt >= :dayStart AND b.bookedAt < :nextDayStart
            """)
    long countBookingsOn(@Param("dayStart") LocalDateTime dayStart,
                         @Param("nextDayStart") LocalDateTime nextDayStart);

    /** Job លុបចោលស្វ័យប្រវត្តិ — {@code PENDING} ដែលហួស ២៤ ម៉ោង។ */
    @EntityGraph(attributePaths = {"schedule"})
    @Query("""
            SELECT b FROM Booking b
            WHERE b.isDeleted = false
              AND b.status = co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING
              AND b.bookedAt < :cutoff
            """)
    List<Booking> findExpiredPending(@Param("cutoff") LocalDateTime cutoff);

    /** UC7.6 — ADMIN មើលការកក់ទាំងអស់ ត្រងតាមស្ថានភាព និងចន្លោះថ្ងៃចេញដំណើរ។ */
    @EntityGraph(attributePaths = {"customer", "schedule", "schedule.tour"})
    @Query("""
            SELECT b FROM Booking b
            WHERE b.isDeleted = false
              AND (:status IS NULL OR b.status = :status)
              AND (CAST(:fromDate AS date) IS NULL OR b.schedule.departureDate >= :fromDate)
              AND (CAST(:toDate   AS date) IS NULL OR b.schedule.departureDate <= :toDate)
            """)
    Page<Booking> search(@Param("status") BookingStatus status,
                         @Param("fromDate") LocalDate fromDate,
                         @Param("toDate") LocalDate toDate,
                         Pageable pageable);
}
