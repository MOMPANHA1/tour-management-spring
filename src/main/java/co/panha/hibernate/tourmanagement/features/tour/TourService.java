package co.panha.hibernate.tourmanagement.features.tour;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.CreateTourRequest;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourCardResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourDetailResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourFilter;
import co.panha.hibernate.tourmanagement.features.tour.dto.UpdateTourRequest;

import java.util.List;

/**
 * សេវាកម្ម Tour — F4។
 */
public interface TourService {

    /** UC4.1 — បង្កើត Tour ថ្មី (ជា draft ជានិច្ច)។ */
    TourDetailResponse createNew(CreateTourRequest request);

    /** UC4.2 — ស្វែងរកតាម filter + sort + page។ */
    PageResponse<TourCardResponse> search(TourFilter filter, Integer page, Integer size);

    /** UC4.3 — មើលលម្អិត Tour។ */
    TourDetailResponse findByUuid(String uuid);

    /** UC4.4 — កែ Tour (PATCH)។ */
    TourDetailResponse updateByUuid(String uuid, UpdateTourRequest request);

    /** UC4.5 — បិទ/បើកលក់។ */
    TourDetailResponse publish(String uuid, boolean shouldPublish);

    /** UC4.6 — លុប Tour (soft delete)។ */
    void deleteByUuid(String uuid);

    /** UC4.7 — Tour ពេញនិយម។ */
    List<TourCardResponse> findPopular(Integer limit);
}
