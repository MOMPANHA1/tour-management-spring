package co.panha.hibernate.tourmanagement.features.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByIdAndIsDeletedFalse(Long id);

    /** ស្ពានពី JWT មកតារាងនេះ — ប្រើក្នុង {@code /me}។ */
    Optional<Customer> findByKeycloakIdAndIsDeletedFalse(String keycloakId);

    /**
     * ពិនិត្យស្ទួន <b>ដោយមិនច្រោះ {@code isDeleted}</b> — ដូចលំនាំ {@code GuideRepository}។
     *
     * <p>អតិថិជនដែលលុប (soft) នៅតែកាន់កាប់ unique constraint។ បើច្រោះចេញ ការចុះឈ្មោះថ្មីនឹង
     * បរាជ័យដោយ {@code DataIntegrityViolationException} (សារទូទៅ) ជំនួសឲ្យសារច្បាស់លាស់។
     */
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<Customer> findAllByIsDeletedFalse(Pageable pageable);

    Page<Customer> findAllByStatusAndIsDeletedFalse(CustomerStatus status, Pageable pageable);

    /** UC6.4 — ស្វែងរកតាមឈ្មោះ អ៊ីមែល ឬលេខទូរស័ព្ទ។ */
    @Query("""
            SELECT c FROM Customer c
            WHERE c.isDeleted = false
              AND (LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR c.phoneNumber     LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<Customer> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
