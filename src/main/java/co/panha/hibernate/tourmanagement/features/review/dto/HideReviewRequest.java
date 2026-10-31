package co.panha.hibernate.tourmanagement.features.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// UC9.6 - reason is required for later audit.
public record HideReviewRequest(

        @NotBlank(message = "Reason is required")
        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        String reason
) {
}
