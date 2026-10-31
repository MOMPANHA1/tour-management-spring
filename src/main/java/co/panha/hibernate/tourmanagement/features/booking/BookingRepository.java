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

    // EntityGraph avoids N+1 since the response always needs tour and schedule.
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

    // Occupied seats; PENDING counts too (holds seats until auto-cancel); COALESCE since SUM of nothing is null.
    @Query("""
            SELECT COALESCE(SUM(b.numberOfPeople), 0) FROM Booking b
            WHERE b.schedule.id = :scheduleId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.COMPLETED)
            """)
    int countOccupiedSeats(@Param("scheduleId") Long scheduleId);

    // BR5: a customer cannot book the same schedule twice.
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

    // Used by F4 Tour: a tour with active bookings cannot be deleted.
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.schedule.tour.id = :tourId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED)
            """)
    long countActiveBookingsByTour(@Param("tourId") Long tourId);

    // Used by F6 Customer: suspending a customer with active bookings requires a reason.
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.customer.id = :customerId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED)
            """)
    long countActiveByCustomer(@Param("customerId") Long customerId);

    // Used by F5 Schedule: cancel all active bookings when a schedule is cancelled.
    @EntityGraph(attributePaths = {"customer"})
    @Query("""
            SELECT b FROM Booking b
            WHERE b.schedule.id = :scheduleId
              AND b.isDeleted = false
              AND b.status IN (co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.booking.BookingStatus.CONFIRMED)
            """)
    List<Booking> findActiveBySchedule(@Param("scheduleId") Long scheduleId);

    // Used by F5 Schedule: CONFIRMED bookings become COMPLETED when the trip ends.
    List<Booking> findByScheduleIdAndStatusAndIsDeletedFalse(Long scheduleId, BookingStatus status);

    // Daily booking count for sequential codes; a range is used because DATE() is non-standard JPQL and defeats the index.
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.bookedAt >= :dayStart AND b.bookedAt < :nextDayStart
            """)
    long countBookingsOn(@Param("dayStart") LocalDateTime dayStart,
                         @Param("nextDayStart") LocalDateTime nextDayStart);

    // Auto-cancel job: PENDING bookings older than 24 hours.
    @EntityGraph(attributePaths = {"schedule"})
    @Query("""
            SELECT b FROM Booking b
            WHERE b.isDeleted = false
              AND b.status = co.panha.hibernate.tourmanagement.features.booking.BookingStatus.PENDING
              AND b.bookedAt < :cutoff
            """)
    List<Booking> findExpiredPending(@Param("cutoff") LocalDateTime cutoff);

    // UC7.6: ADMIN search filtered by status and departure date range.
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
