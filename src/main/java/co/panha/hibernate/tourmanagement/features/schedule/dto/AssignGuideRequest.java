package co.panha.hibernate.tourmanagement.features.schedule.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ចាត់តាំងមគ្គុទ្ទេសក៍ទៅកាលវិភាគ — UC5.4។
 */
public record AssignGuideRequest(

        @NotBlank(message = "Guide is required")
        String guideUuid
) {
}
