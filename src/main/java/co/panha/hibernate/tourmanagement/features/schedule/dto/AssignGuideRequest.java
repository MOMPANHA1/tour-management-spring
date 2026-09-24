package co.panha.hibernate.tourmanagement.features.schedule.dto;

import jakarta.validation.constraints.NotNull;

/**
 * ចាត់តាំងមគ្គុទ្ទេសក៍ទៅកាលវិភាគ — UC5.4។
 */
public record AssignGuideRequest(

        @NotNull(message = "Guide is required")
        Long guideId
) {
}
