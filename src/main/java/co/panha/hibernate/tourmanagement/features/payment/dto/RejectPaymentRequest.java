package co.panha.hibernate.tourmanagement.features.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** ហេតុផលបដិសេធការទូទាត់ — UC8.4។ ចាំបាច់ ដើម្បីឲ្យអតិថិជនដឹងថាត្រូវធ្វើអ្វីបន្ត។ */
public record RejectPaymentRequest(

        @NotBlank(message = "Reject reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
