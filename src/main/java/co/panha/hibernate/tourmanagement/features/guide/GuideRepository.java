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

    Optional<Guide> findByIdAndIsDeletedFalse(Long id);

    // Does not filter isDeleted: soft-deleted guides still hold the unique phone, so a clear 409 message is needed.
    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByCode(String code);

    Page<Guide> findAllByIsDeletedFalse(Pageable pageable);

    Page<Guide> findAllByStatusAndIsDeletedFalse(GuideStatus status, Pageable pageable);

    long countByIsDeletedFalse();

    // UC3.6 - active guides not in the busy list.
    List<Guide> findAllByStatusAndIsDeletedFalseAndIdNotIn(GuideStatus status, Collection<Long> busyIds);

    List<Guide> findAllByStatusAndIsDeletedFalse(GuideStatus status);
}
