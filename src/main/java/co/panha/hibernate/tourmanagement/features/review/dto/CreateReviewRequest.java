package co.panha.hibernate.tourmanagement.features.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// UC9.1 - tourId/customerId are derived from the booking, never accepted from the client.
public record CreateReviewRequest(

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        Integer rating,

        @Min(value = 1, message = "Guide rating must be between 1 and 5")
        @Max(value = 5, message = "Guide rating must be between 1 and 5")
        Integer guideRating,

        @Min(value = 1, message = "Value rating must be between 1 and 5")
        @Max(value = 5, message = "Value rating must be between 1 and 5")
        Integer valueRating,

        @Size(max = 180, message = "Title cannot exceed 180 characters")
        String title,

        @NotBlank(message = "Comment is required")
        @Size(min = 10, max = 3000, message = "Comment must be between 10 and 3000 characters")
        String comment,

        @Size(max = 5, message = "At most 5 images are allowed")
        List<@Size(max = 255, message = "Image URL cannot exceed 255 characters") String> imageUrls
) {
}
