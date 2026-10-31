package co.panha.hibernate.tourmanagement.features.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// UC5.5: reason is required because it is sent to every customer who booked this schedule.
public record CancelScheduleRequest(

        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
