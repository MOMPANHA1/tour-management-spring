package co.panha.hibernate.tourmanagement.features.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// UC7.4 cancellation reason; required because it affects refunds.
public record CancelBookingRequest(

        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
