package co.panha.hibernate.tourmanagement.features.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** ការឆ្លើយតបដោយ ADMIN — UC9.5។ */
public record ReplyReviewRequest(

        @NotBlank(message = "Reply is required")
        @Size(max = 2000, message = "Reply cannot exceed 2000 characters")
        String reply
) {
}
