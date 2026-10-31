package co.panha.hibernate.tourmanagement.features.schedule;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<TourSchedule, Long> {

    @EntityGraph(attributePaths = {"tour", "guide"})
    Optional<TourSchedule> findByIdAndIsDeletedFalse(Long id);

    boolean existsByCode(String code);

    List<TourSchedule> findByStatusInAndDepartureDateBeforeAndIsDeletedFalse(
            List<ScheduleStatus> statuses, LocalDate date);

    List<TourSchedule> findByStatusAndReturnDateBeforeAndIsDeletedFalse(
            ScheduleStatus status, LocalDate date);

    // Pessimistic lock for the transaction so concurrent bookings cannot read stale seat counts (F7).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TourSchedule s WHERE s.id = :id AND s.isDeleted = false")
    Optional<TourSchedule> findByIdForUpdate(@Param("id") Long id);

    // UC5.2: schedules still open for booking for one tour.
    @EntityGraph(attributePaths = {"tour", "guide"})
    @Query("""
            SELECT s FROM TourSchedule s
            WHERE s.tour.id = :tourId
              AND s.isDeleted = false
              AND s.departureDate >= :fromDate
              AND s.status IN (co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.OPEN,
                               co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.FULL)
            ORDER BY s.departureDate ASC
            """)
    List<TourSchedule> findOpenSchedulesByTour(@Param("tourId") Long tourId,
                                               @Param("fromDate") LocalDate fromDate);

    // Used by F4: a tour can be published only if it has at least one open schedule.
    @Query("""
            SELECT COUNT(s) FROM TourSchedule s
            WHERE s.tour.id = :tourId
              AND s.isDeleted = false
              AND s.departureDate >= :fromDate
              AND s.status IN (co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.OPEN,
                               co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.FULL)
            """)
    long countOpenSchedules(@Param("tourId") Long tourId, @Param("fromDate") LocalDate fromDate);

    // Used by F3: a guide cannot be set INACTIVE while future schedules remain.
    @Query("""
            SELECT COUNT(s) FROM TourSchedule s
            WHERE s.guide.id = :guideId
              AND s.isDeleted = false
              AND s.departureDate >= :fromDate
              AND s.status NOT IN (co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.CANCELLED,
                                   co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.COMPLETED)
            """)
    long countUpcomingByGuide(@Param("guideId") Long guideId, @Param("fromDate") LocalDate fromDate);

    // Total schedules assigned to one guide (F3).
    long countByGuideIdAndIsDeletedFalse(Long guideId);

    // True if the guide has an overlapping schedule; excludeScheduleId skips the schedule being edited.
    @Query("""
            SELECT COUNT(s) > 0 FROM TourSchedule s
            WHERE s.guide.id = :guideId
              AND s.isDeleted = false
              AND s.status <> co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.CANCELLED
              AND (:excludeScheduleId IS NULL OR s.id <> :excludeScheduleId)
              AND s.departureDate <= :endDate
              AND s.returnDate    >= :startDate
            """)
    boolean hasGuideConflict(@Param("guideId") Long guideId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             @Param("excludeScheduleId") Long excludeScheduleId);

    // Next departure date of a tour, used for nextDepartureDate on tour cards (F4).
    @Query("""
            SELECT MIN(s.departureDate) FROM TourSchedule s
            WHERE s.tour.id = :tourId
              AND s.isDeleted = false
              AND s.departureDate >= :fromDate
              AND s.status IN (co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.OPEN,
                               co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.FULL)
            """)
    LocalDate findNextDepartureDate(@Param("tourId") Long tourId, @Param("fromDate") LocalDate fromDate);

    // Used by F3 UC3.6: guides busy within the date range.
    @Query("""
            SELECT DISTINCT s.guide.id FROM TourSchedule s
            WHERE s.guide IS NOT NULL
              AND s.isDeleted = false
              AND s.status <> co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.CANCELLED
              AND s.departureDate <= :endDate
              AND s.returnDate    >= :startDate
            """)
    List<Long> findBusyGuideIds(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}
