package co.panha.hibernate.tourmanagement.features.tour;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.CreateTourRequest;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourCardResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourDetailResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourFilter;
import co.panha.hibernate.tourmanagement.features.tour.dto.UpdateTourRequest;

import java.util.List;

// Tour service (F4).
public interface TourService {

    // UC4.1: create a tour (always as draft).
    TourDetailResponse createNew(CreateTourRequest request);

    // UC4.2: search by filter, sort and page.
    PageResponse<TourCardResponse> search(TourFilter filter, Integer page, Integer size);

    // UC4.3: tour detail.
    TourDetailResponse findById(Long id);

    // UC4.4: update a tour (PATCH).
    TourDetailResponse updateById(Long id, UpdateTourRequest request);

    // UC4.5: publish or unpublish.
    TourDetailResponse publish(Long id, boolean shouldPublish);

    // UC4.6: delete a tour (soft delete).
    void deleteById(Long id);

    // UC4.7: popular tours.
    List<TourCardResponse> findPopular(Integer limit);
}
