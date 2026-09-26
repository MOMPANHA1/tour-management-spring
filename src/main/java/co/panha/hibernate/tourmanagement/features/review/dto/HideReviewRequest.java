package co.panha.hibernate.tourmanagement.features.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** ការលាក់ការវាយតម្លៃមិនសមរម្យ — UC9.6។ ហេតុផលចាំបាច់ សម្រាប់ការត្រួតពិនិត្យក្រោយ។ */
public record HideReviewRequest(

        @NotBlank(message = "Reason is required")
        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        String reason
) {
}
