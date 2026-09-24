package co.panha.hibernate.tourmanagement.features.destination;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findByIdAndIsDeletedFalse(Long id);

    /**
     * ពិនិត្យស្ទួន <b>ដោយមិនច្រោះ {@code isDeleted}</b> — ព្រោះ unique constraint
     * {@code (name, province)} ក្នុង database អនុវត្តលើជួរដែលលុប (soft) ដែរ។
     */
    boolean existsByNameIgnoreCaseAndProvinceIgnoreCase(String name, String province);

    Page<Destination> findAllByIsDeletedFalse(Pageable pageable);

    Page<Destination> findAllByProvinceIgnoreCaseAndIsDeletedFalse(String province, Pageable pageable);

    /** ប្រើនៅ F4 ពេល Tour ភ្ជាប់ទីតាំងច្រើនតាម id។ */
    List<Destination> findAllByIdInAndIsDeletedFalse(List<Long> ids);
}
