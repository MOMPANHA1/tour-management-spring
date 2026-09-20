package co.panha.hibernate.tourmanagement.features.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * បោះបង់កាលវិភាគ — UC5.5។
 *
 * <p>មូលហេតុចាំបាច់ ព្រោះវាបញ្ជូនទៅអតិថិជនគ្រប់រូបដែលកក់កាលវិភាគនេះ។
 */
public record CancelScheduleRequest(

        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
