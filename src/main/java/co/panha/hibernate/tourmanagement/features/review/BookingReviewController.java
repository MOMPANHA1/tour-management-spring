package co.panha.hibernate.tourmanagement.features.review;

import co.panha.hibernate.tourmanagement.features.review.dto.CreateReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC9.1 — ការវាយតម្លៃជាធនធានរងរបស់ការកក់ ព្រោះវាយតម្លៃបានតែលើការកក់ដែលបញ្ចប់។
 */
@Tag(name = "Review")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings/{code}")
public class BookingReviewController {

    private final ReviewService reviewService;

    @PostMapping("/review")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createNew(@PathVariable String code,
                                    @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createNew(code, request);
    }
}
