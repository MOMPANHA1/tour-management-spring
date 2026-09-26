package co.panha.hibernate.tourmanagement.features.review;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.CreateReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.HideReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReplyReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewSummaryResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.UpdateReviewRequest;

/**
 * សេវាកម្មការវាយតម្លៃ — F9។
 */
public interface ReviewService {

    /** UC9.1 — អតិថិជនផ្តល់ការវាយតម្លៃលើការកក់ដែលបញ្ចប់។ */
    ReviewResponse createNew(String bookingCode, CreateReviewRequest request);

    /** UC9.2 — ការវាយតម្លៃសាធារណៈរបស់ Tour មួយ។ */
    PageResponse<ReviewResponse> findByTour(Long tourId, Integer page, Integer size);

    /** UC9.7 — សង្ខេបពិន្ទុ (រាប់តាមផ្កាយ)។ */
    ReviewSummaryResponse getSummary(Long tourId);

    /** UC9.3 — អតិថិជនកែការវាយតម្លៃរបស់ខ្លួន។ */
    ReviewResponse updateById(Long id, UpdateReviewRequest request);

    /** UC9.4 — អតិថិជនលុបការវាយតម្លៃរបស់ខ្លួន (soft delete)។ */
    void deleteById(Long id);

    /** UC9.5 — ADMIN ឆ្លើយតប។ */
    ReviewResponse reply(Long id, ReplyReviewRequest request);

    /** UC9.6 — ADMIN លាក់ការវាយតម្លៃមិនសមរម្យ។ */
    ReviewResponse hide(Long id, HideReviewRequest request);
}
