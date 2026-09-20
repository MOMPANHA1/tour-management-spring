package co.panha.hibernate.tourmanagement.features.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByUuidAndIsDeletedFalse(String uuid);

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    /**
     * ពិនិត្យ slug <b>ដោយមិនច្រោះ {@code isDeleted}</b> ដោយចេតនា។
     *
     * <p>ជួរដែលលុប (soft) នៅតែកាន់កាប់ unique constraint ក្នុង database។ បើច្រោះវាចេញ
     * ការបង្កើតថ្មីនឹងបរាជ័យដោយ {@code DataIntegrityViolationException} ជំនួសឲ្យទទួល slug ថ្មី។
     */
    boolean existsBySlug(String slug);

    Page<Category> findAllByIsDeletedFalse(Pageable pageable);
}
