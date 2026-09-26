package co.panha.hibernate.tourmanagement.features.review;

import co.panha.hibernate.tourmanagement.features.review.dto.HideReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReplyReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.UpdateReviewRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * ការគ្រប់គ្រងការវាយតម្លៃម្តងមួយៗ — UC9.3 ដល់ UC9.6។
 */
@Tag(name = "Review")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PatchMapping("/{id}")
    public ReviewResponse updateById(@PathVariable Long id,
                                     @Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.updateById(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        reviewService.deleteById(id);
    }

    @PostMapping("/{id}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse reply(@PathVariable Long id,
                                @Valid @RequestBody ReplyReviewRequest request) {
        return reviewService.reply(id, request);
    }

    @PatchMapping("/{id}/hide")
    public ReviewResponse hide(@PathVariable Long id,
                               @Valid @RequestBody HideReviewRequest request) {
        return reviewService.hide(id, request);
    }
}
