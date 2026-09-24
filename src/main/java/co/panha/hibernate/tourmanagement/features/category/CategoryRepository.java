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

    /**
     * ពិនិត្យ slug ក្នុងចំណោម<b>ជួរដែលនៅរស់</b>តែប៉ុណ្ណោះ។
     *
     * <p>ច្រោះ {@code isDeleted} បានព្រោះវិន័យ unique ក្នុង database ជា partial index
     * {@code ux_categories_slug_active ... WHERE is_deleted = false} — ជួរដែលលុប (soft)
     * លែងកាន់កាប់ slug ទេ។ បើថ្ងៃណាមួយ index នោះត្រូវប្តូរមកជា unique constraint ធម្មតាវិញ
     * method នេះត្រូវដកការច្រោះចេញ បើមិនដូច្នេះការបង្កើតថ្មីនឹងបរាជ័យដោយ
     * {@code DataIntegrityViolationException} ជំនួសឲ្យទទួល slug ថ្មី។
     */
    boolean existsBySlugAndIsDeletedFalse(String slug);

    Page<Category> findAllByIsDeletedFalse(Pageable pageable);
}
