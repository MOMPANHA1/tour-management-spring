package co.panha.hibernate.tourmanagement.features.tour.dto;

import jakarta.validation.constraints.NotNull;

// UC4.5: Boolean (not boolean) so @NotNull catches a missing field instead of silently unpublishing.
public record PublishTourRequest(

        @NotNull(message = "Published flag is required")
        Boolean published
) {
}
