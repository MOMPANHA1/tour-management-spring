package co.panha.hibernate.tourmanagement.features.guide;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.CreateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.GuideResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideStatusRequest;

import java.time.LocalDate;
import java.util.List;

// Guide service (F3).
public interface GuideService {

    // UC3.1 - create a guide.
    GuideResponse createNew(CreateGuideRequest request);

    // UC3.2 - list guides, optionally filtered by status.
    PageResponse<GuideResponse> findAll(GuideStatus status, Integer page, Integer size);

    // UC3.6 - guides available in a date range.
    List<GuideResponse> findAvailable(LocalDate startDate, LocalDate endDate);

    // UC3.3 - guide profile.
    GuideResponse findById(Long id);

    // UC3.4 - PATCH guide info.
    GuideResponse updateById(Long id, UpdateGuideRequest request);

    // UC3.5 - change status.
    GuideResponse changeStatus(Long id, UpdateGuideStatusRequest request);

    // Soft delete a guide.
    void deleteById(Long id);
}
