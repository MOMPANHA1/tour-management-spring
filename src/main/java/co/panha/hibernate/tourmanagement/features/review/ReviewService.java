package co.panha.hibernate.tourmanagement.features.review;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.CreateReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.HideReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReplyReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewSummaryResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.UpdateReviewRequest;

// Review service (F9).
public interface ReviewService {

    // UC9.1 - customer reviews a completed booking.
    ReviewResponse createNew(String bookingCode, CreateReviewRequest request);

    // UC9.2 - public reviews of one tour.
    PageResponse<ReviewResponse> findByTour(Long tourId, Integer page, Integer size);

    // UC9.7 - rating summary (counts per star).
    ReviewSummaryResponse getSummary(Long tourId);

    // UC9.3 - customer edits own review.
    ReviewResponse updateById(Long id, UpdateReviewRequest request);

    // UC9.4 - customer deletes own review (soft delete).
    void deleteById(Long id);

    // UC9.5 - ADMIN reply.
    ReviewResponse reply(Long id, ReplyReviewRequest request);

    // UC9.6 - ADMIN hides an inappropriate review.
    ReviewResponse hide(Long id, HideReviewRequest request);
}
