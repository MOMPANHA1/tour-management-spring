package co.panha.hibernate.tourmanagement.features.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// UC8.4 rejection reason; required so the customer knows what to do next.
public record RejectPaymentRequest(

        @NotBlank(message = "Reject reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
