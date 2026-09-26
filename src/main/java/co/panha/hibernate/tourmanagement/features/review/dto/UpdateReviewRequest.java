package co.panha.hibernate.tourmanagement.features.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * កែការវាយតម្លៃ — UC9.3 (PATCH)។
 *
 * <p>គ្រប់ field អាច {@code null} — មានន័យថា "កុំប៉ះ field នេះ"។
 * {@code imageUrls} ជាការ<b>ជំនួសពេញ</b> មិនមែនការបន្ថែមទេ។
 */
public record UpdateReviewRequest(

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

        @Size(min = 10, max = 3000, message = "Comment must be between 10 and 3000 characters")
        String comment,

        @Size(max = 5, message = "At most 5 images are allowed")
        List<@Size(max = 255, message = "Image URL cannot exceed 255 characters") String> imageUrls
) {
}
