package co.panha.hibernate.tourmanagement.features.destination;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.CreateDestinationRequest;
import co.panha.hibernate.tourmanagement.features.destination.dto.DestinationResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.UpdateDestinationRequest;

// Destination service (F2).
public interface DestinationService {

    // UC2.1 - create a destination.
    DestinationResponse createNew(CreateDestinationRequest request);

    // UC2.2 - list destinations, optionally filtered by province.
    PageResponse<DestinationResponse> findAll(String province, Integer page, Integer size);

    // UC2.3 - get one destination by id.
    DestinationResponse findById(Long id);

    // UC2.4 - PATCH a destination.
    DestinationResponse updateById(Long id, UpdateDestinationRequest request);

    // UC2.5 - soft delete a destination.
    void deleteById(Long id);
}
