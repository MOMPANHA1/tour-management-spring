package co.panha.hibernate.tourmanagement.features.guide;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {

    Optional<Guide> findByUuidAndIsDeletedFalse(String uuid);

    /**
     * ពិនិត្យលេខទូរស័ព្ទ <b>ដោយមិនច្រោះ {@code isDeleted}</b>។
     *
     * <p>មគ្គុទ្ទេសក៍ដែលលុប (soft) នៅតែកាន់កាប់ unique constraint។ បើច្រោះចេញ ការបង្កើតថ្មីនឹង
     * បរាជ័យដោយ {@code DataIntegrityViolationException} (409 សារទូទៅ) ជំនួសឲ្យសារខ្មែរច្បាស់លាស់។
     */
    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByCode(String code);

    Page<Guide> findAllByIsDeletedFalse(Pageable pageable);

    Page<Guide> findAllByStatusAndIsDeletedFalse(GuideStatus status, Pageable pageable);

    long countByIsDeletedFalse();

    /** UC3.6 — មគ្គុទ្ទេសក៍សកម្មដែលមិននៅក្នុងបញ្ជីរវល់។ */
    List<Guide> findAllByStatusAndIsDeletedFalseAndIdNotIn(GuideStatus status, Collection<Long> busyIds);

    List<Guide> findAllByStatusAndIsDeletedFalse(GuideStatus status);
}
