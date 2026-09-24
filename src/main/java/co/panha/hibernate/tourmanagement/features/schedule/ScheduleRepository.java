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

    /**
     * ចាក់សោជួរពេញរយៈពេល transaction — ការពារ race condition ពេលកក់ (F7)។
     *
     * <p>ពេលមនុស្ស ១០ នាក់ចុច [កក់] ព្រមគ្នាលើកៅអី ៨ គេត្រូវរង់ចាំគ្នាម្នាក់ម្តង
     * ដូច្នេះការរាប់កៅអីមិនអាចអានទិន្នន័យចាស់ទេ។
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TourSchedule s WHERE s.id = :id AND s.isDeleted = false")
    Optional<TourSchedule> findByIdForUpdate(@Param("id") Long id);

    /** UC5.2 — កាលវិភាគដែលនៅបើកទទួលការកក់សម្រាប់ Tour មួយ។ */
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

    /** ប្រើដោយ F4 — Tour បើកលក់បានលុះត្រាតែមានកាលវិភាគបើកយ៉ាងតិច ១។ */
    @Query("""
            SELECT COUNT(s) FROM TourSchedule s
            WHERE s.tour.id = :tourId
              AND s.isDeleted = false
              AND s.departureDate >= :fromDate
              AND s.status IN (co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.OPEN,
                               co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.FULL)
            """)
    long countOpenSchedules(@Param("tourId") Long tourId, @Param("fromDate") LocalDate fromDate);

    /** ប្រើដោយ F3 — មគ្គុទ្ទេសក៍ដាក់ INACTIVE មិនបានបើនៅមានកាលវិភាគអនាគត។ */
    @Query("""
            SELECT COUNT(s) FROM TourSchedule s
            WHERE s.guide.id = :guideId
              AND s.isDeleted = false
              AND s.departureDate >= :fromDate
              AND s.status NOT IN (co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.CANCELLED,
                                   co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.COMPLETED)
            """)
    long countUpcomingByGuide(@Param("guideId") Long guideId, @Param("fromDate") LocalDate fromDate);

    /** ចំនួនកាលវិភាគសរុបដែលចាត់តាំងឲ្យមគ្គុទ្ទេសក៍ម្នាក់ — ប្រើដោយ F3។ */
    long countByGuideIdAndIsDeletedFalse(Long guideId);

    /**
     * មគ្គុទ្ទេសក៍នេះមានកាលវិភាគជាន់គ្នាក្នុងចន្លោះថ្ងៃនេះឬទេ។
     *
     * <p>{@code excludeScheduleId} ប្រើពេលកែកាលវិភាគដែលមានស្រាប់ — កុំឲ្យវាជាន់នឹងខ្លួនឯង។
     */
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

    /** ថ្ងៃចេញដំណើរបន្ទាប់របស់ Tour — បំពេញ {@code nextDepartureDate} លើកាត Tour (F4)។ */
    @Query("""
            SELECT MIN(s.departureDate) FROM TourSchedule s
            WHERE s.tour.id = :tourId
              AND s.isDeleted = false
              AND s.departureDate >= :fromDate
              AND s.status IN (co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.OPEN,
                               co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus.FULL)
            """)
    LocalDate findNextDepartureDate(@Param("tourId") Long tourId, @Param("fromDate") LocalDate fromDate);

    /** ប្រើដោយ F3 UC3.6 — មគ្គុទ្ទេសក៍ដែលរវល់ក្នុងចន្លោះថ្ងៃនេះ។ */
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
