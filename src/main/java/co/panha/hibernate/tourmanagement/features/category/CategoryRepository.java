package co.panha.hibernate.tourmanagement.features.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByIdAndIsDeletedFalse(Long id);

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    // Active rows only: the DB unique rule is a partial index; drop this filter if it becomes a plain unique constraint.
    boolean existsBySlugAndIsDeletedFalse(String slug);

    Page<Category> findAllByIsDeletedFalse(Pageable pageable);
}
