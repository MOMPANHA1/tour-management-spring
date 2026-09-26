package co.panha.hibernate.tourmanagement.features.review;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewSummaryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC9.2 និង UC9.7 — ការវាយតម្លៃសាធារណៈរបស់ Tour មួយ។
 *
 * <p>សាធារណៈទាំងស្រុង — ភ្ញៀវត្រូវអានការវាយតម្លៃបានមុន login។
 */
@Tag(name = "Review")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tours/{tourId}/reviews")
public class TourReviewController {

    private final ReviewService reviewService;

    /** ត្រូវប្រកាស<b>មុន</b> route ទូទៅ ដើម្បីកុំឲ្យ {@code summary} ក្លាយជាប៉ារ៉ាម៉ែត្រ។ */
    @GetMapping("/summary")
    public ReviewSummaryResponse getSummary(@PathVariable Long tourId) {
        return reviewService.getSummary(tourId);
    }

    @GetMapping
    public PageResponse<ReviewResponse> findByTour(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return reviewService.findByTour(tourId, page, size);
    }
}
