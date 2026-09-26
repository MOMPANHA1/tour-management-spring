package co.panha.hibernate.tourmanagement.features.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"booking", "tour", "customer"})
    Optional<Review> findByIdAndIsDeletedFalse(Long id);

    /** BR3 — ការកក់មួយវាយតម្លៃបានតែម្តង។ រាប់ទាំងអ្វីដែលលុបរួច (soft) ដើម្បីកុំឲ្យវាយឡើងវិញ។ */
    boolean existsByBookingId(Long bookingId);

    @EntityGraph(attributePaths = {"booking", "tour", "customer"})
    Optional<Review> findByBookingIdAndIsDeletedFalse(Long bookingId);

    /** UC9.2 — ការវាយតម្លៃសាធារណៈរបស់ Tour មួយ។ */
    @EntityGraph(attributePaths = {"booking", "customer"})
    @Query("""
            SELECT r FROM Review r
            WHERE r.tour.id = :tourId
              AND r.isVisible = true
              AND r.isDeleted = false
            """)
    Page<Review> findVisibleByTour(@Param("tourId") Long tourId, Pageable pageable);

    /**
     * ពិន្ទុមធ្យម — រាប់តែការវាយតម្លៃដែលមើលឃើញ។
     *
     * <p>{@code COALESCE} ចាំបាច់៖ {@code AVG} លើសំណុំទទេត្រឡប់ {@code null}
     * ដែលនឹងបង្ក {@code NullPointerException} ពេល unbox ទៅ {@code double}។
     */
    @Query("""
            SELECT COALESCE(AVG(r.rating), 0) FROM Review r
            WHERE r.tour.id = :tourId AND r.isVisible = true AND r.isDeleted = false
            """)
    Double averageRating(@Param("tourId") Long tourId);

    @Query("""
            SELECT COALESCE(AVG(r.guideRating), 0) FROM Review r
            WHERE r.tour.id = :tourId AND r.guideRating IS NOT NULL
              AND r.isVisible = true AND r.isDeleted = false
            """)
    Double averageGuideRating(@Param("tourId") Long tourId);

    @Query("""
            SELECT COALESCE(AVG(r.valueRating), 0) FROM Review r
            WHERE r.tour.id = :tourId AND r.valueRating IS NOT NULL
              AND r.isVisible = true AND r.isDeleted = false
            """)
    Double averageValueRating(@Param("tourId") Long tourId);

    @Query("""
            SELECT COUNT(r) FROM Review r
            WHERE r.tour.id = :tourId AND r.isVisible = true AND r.isDeleted = false
            """)
    long countVisibleByTour(@Param("tourId") Long tourId);

    /** UC9.7 — ចំនួនតាមផ្កាយនីមួយៗ។ ត្រឡប់ {@code [rating, count]} តែសម្រាប់ផ្កាយដែលមាន។ */
    @Query("""
            SELECT r.rating, COUNT(r) FROM Review r
            WHERE r.tour.id = :tourId AND r.isVisible = true AND r.isDeleted = false
            GROUP BY r.rating
            """)
    List<Object[]> countByStars(@Param("tourId") Long tourId);
}
