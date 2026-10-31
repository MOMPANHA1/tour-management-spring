package co.panha.hibernate.tourmanagement.features.schedule.dto;

import jakarta.validation.constraints.NotNull;

// UC5.4: assign a guide to a schedule.
public record AssignGuideRequest(

        @NotNull(message = "Guide is required")
        Long guideId
) {
}
