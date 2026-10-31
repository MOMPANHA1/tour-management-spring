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

    // Bridge from the JWT to this table; used by /me.
    Optional<Customer> findByKeycloakIdAndIsDeletedFalse(String keycloakId);

    // Duplicate check intentionally ignores isDeleted: soft-deleted rows still hold the unique constraints.
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<Customer> findAllByIsDeletedFalse(Pageable pageable);

    Page<Customer> findAllByStatusAndIsDeletedFalse(CustomerStatus status, Pageable pageable);

    // UC6.4: search by name, email or phone.
    @Query("""
            SELECT c FROM Customer c
            WHERE c.isDeleted = false
              AND (LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR c.phoneNumber     LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<Customer> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
