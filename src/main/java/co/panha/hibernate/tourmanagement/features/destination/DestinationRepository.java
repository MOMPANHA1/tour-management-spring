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

    // Does not filter isDeleted because the (name, province) unique constraint also covers soft-deleted rows.
    boolean existsByNameIgnoreCaseAndProvinceIgnoreCase(String name, String province);

    Page<Destination> findAllByIsDeletedFalse(Pageable pageable);

    Page<Destination> findAllByProvinceIgnoreCaseAndIsDeletedFalse(String province, Pageable pageable);

    // Used by F4 when a tour links several destinations by id.
    List<Destination> findAllByIdInAndIsDeletedFalse(List<Long> ids);
}
