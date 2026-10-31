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
    Optional<Tour> findByIdAndIsDeletedFalse(Long id);

    Optional<Tour> findBySlugAndIsDeletedFalse(String slug);

    boolean existsBySlug(String slug);

    boolean existsByCode(String code);

    long countByIsDeletedFalse();

    // Fetch category eagerly to avoid N+1 queries from TourCardResponse.categoryName.
    @Override
    @EntityGraph(attributePaths = {"category"})
    Page<Tour> findAll(Specification<Tour> spec, Pageable pageable);

    // UC4.7: popular tours (need at least 3 ratings to be listed).
    @EntityGraph(attributePaths = {"category"})
    @Query("""
            SELECT t FROM Tour t
            WHERE t.isDeleted = false
              AND t.isPublished = true
              AND t.reviewCount >= 3
            ORDER BY t.averageRating DESC, t.reviewCount DESC
            """)
    Page<Tour> findPopular(Pageable pageable);

    // Active tours in one category; used by F1.
    @Query("SELECT COUNT(t) FROM Tour t WHERE t.category.id = :categoryId AND t.isDeleted = false")
    long countActiveByCategory(@Param("categoryId") Long categoryId);

    // Active tours linked to one destination; used by F2.
    @Query("""
            SELECT COUNT(t) FROM Tour t
            JOIN t.destinations d
            WHERE d.id = :destinationId AND t.isDeleted = false
            """)
    long countActiveByDestination(@Param("destinationId") Long destinationId);

    // Note: rating fields are recalculated in ReviewServiceImpl.recalculateTourRating() via entity save, not bulk update, to keep the persistence context fresh.
}
