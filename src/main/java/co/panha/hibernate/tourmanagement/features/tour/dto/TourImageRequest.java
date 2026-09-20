package co.panha.hibernate.tourmanagement.features.tour.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TourImageRequest(

        @NotBlank(message = "Image URL is required")
        @Size(max = 255, message = "Image URL cannot exceed 255 characters")
        String url,

        @Size(max = 180, message = "Caption cannot exceed 180 characters")
        String caption,

        @Min(value = 0, message = "Sort order cannot be negative")
        Integer sortOrder
) {
}
