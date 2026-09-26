package co.panha.hibernate.tourmanagement.features.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** ហេតុផលលុបចោល — UC7.4។ ចាំបាច់ ព្រោះវាប៉ះពាល់ដល់ការសងប្រាក់វិញ។ */
public record CancelBookingRequest(

        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
