package co.panha.hibernate.tourmanagement.features.tour;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {

    @EntityGraph(attributePaths = {"category"})
    Optional<Tour> findByUuidAndIsDeletedFalse(String uuid);

    Optional<Tour> findBySlugAndIsDeletedFalse(String slug);

    boolean existsBySlug(String slug);

    boolean existsByCode(String code);

    long countByIsDeletedFalse();

    /**
     * ទាញ {@code category} មកជាមួយ ដើម្បីកុំឲ្យ {@code TourCardResponse.categoryName}
     * បង្ក N+1 query (ទំព័រ ១២ Tour → ១៣ query បើគ្មាន)។
     */
    @Override
    @EntityGraph(attributePaths = {"category"})
    Page<Tour> findAll(Specification<Tour> spec, Pageable pageable);

    /** UC4.7 — Tour ពេញនិយម (ត្រូវមានវាយតម្លៃយ៉ាងតិច ៣ ទើបចូលបញ្ជី)។ */
    @EntityGraph(attributePaths = {"category"})
    @Query("""
            SELECT t FROM Tour t
            WHERE t.isDeleted = false
              AND t.isPublished = true
              AND t.reviewCount >= 3
            ORDER BY t.averageRating DESC, t.reviewCount DESC
            """)
    Page<Tour> findPopular(Pageable pageable);

    /** ចំនួន Tour សកម្មក្នុងប្រភេទមួយ — ប្រើដោយ F1 Category។ */
    @Query("SELECT COUNT(t) FROM Tour t WHERE t.category.id = :categoryId AND t.isDeleted = false")
    long countActiveByCategory(@Param("categoryId") Long categoryId);

    /** ចំនួន Tour សកម្មដែលភ្ជាប់នឹងទីតាំងមួយ — ប្រើដោយ F2 Destination។ */
    @Query("""
            SELECT COUNT(t) FROM Tour t
            JOIN t.destinations d
            WHERE d.id = :destinationId AND t.isDeleted = false
            """)
    long countActiveByDestination(@Param("destinationId") Long destinationId);

    // TODO ដំណាក់កាល ៣ (F9 Review)៖ គណនា averageRating និង reviewCount ឡើងវិញ
    //   @Modifying
    //   @Query("""
    //           UPDATE Tour t SET
    //              t.averageRating = (SELECT COALESCE(AVG(r.rating), 0) FROM Review r
    //                                 WHERE r.tour.id = :tourId AND r.isDeleted = false),
    //              t.reviewCount   = (SELECT COUNT(r) FROM Review r
    //                                 WHERE r.tour.id = :tourId AND r.isDeleted = false)
    //           WHERE t.id = :tourId
    //           """)
    //   void recalculateRating(@Param("tourId") Long tourId);
}
