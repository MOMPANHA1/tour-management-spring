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

// UC9.2 and UC9.7 - public tour reviews, readable before login.
@Tag(name = "Review")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tours/{tourId}/reviews")
public class TourReviewController {

    private final ReviewService reviewService;

    // Declared before the generic route so 'summary' is not bound as a path parameter.
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
