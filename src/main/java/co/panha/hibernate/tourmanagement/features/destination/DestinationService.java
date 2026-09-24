package co.panha.hibernate.tourmanagement.features.destination;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.CreateDestinationRequest;
import co.panha.hibernate.tourmanagement.features.destination.dto.DestinationResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.UpdateDestinationRequest;

/**
 * សេវាកម្មទីតាំងគោលដៅ — F2។
 */
public interface DestinationService {

    /** UC2.1 — បង្កើតទីតាំងថ្មី។ */
    DestinationResponse createNew(CreateDestinationRequest request);

    /** UC2.2 — បញ្ជីទីតាំង ត្រងតាមខេត្តបាន។ */
    PageResponse<DestinationResponse> findAll(String province, Integer page, Integer size);

    /** UC2.3 — ទីតាំងមួយតាម id។ */
    DestinationResponse findById(Long id);

    /** UC2.4 — កែទីតាំង (PATCH)។ */
    DestinationResponse updateById(Long id, UpdateDestinationRequest request);

    /** UC2.5 — លុបទីតាំង (soft delete)។ */
    void deleteById(Long id);
}
