package co.panha.hibernate.tourmanagement.features.tour.dto;

import co.panha.hibernate.tourmanagement.features.tour.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

// UC4.1: code, slug and isPublished are system-set; cross-field rules are checked in the service.
public record CreateTourRequest(

        @NotBlank(message = "Tour title is required")
        @Size(max = 180, message = "Tour title cannot exceed 180 characters")
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @Size(max = 10000, message = "Itinerary cannot exceed 10000 characters")
        String itinerary,

        @Size(max = 3000, message = "Included section cannot exceed 3000 characters")
        String included,

        @Size(max = 3000, message = "Excluded section cannot exceed 3000 characters")
        String excluded,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Duration in days is required")
        @Min(value = 1, message = "Duration must be at least 1 day")
        @Max(value = 90, message = "Duration cannot exceed 90 days")
        Integer durationDays,

        @Min(value = 0, message = "Nights cannot be negative")
        @Max(value = 90, message = "Nights cannot exceed 90")
        Integer durationNights,

        @Min(value = 1, message = "Minimum group size must be at least 1")
        Integer minGroupSize,

        @NotNull(message = "Maximum group size is required")
        @Min(value = 1, message = "Maximum group size must be at least 1")
        @Max(value = 200, message = "Maximum group size cannot exceed 200")
        Integer maxGroupSize,

        @NotNull(message = "Difficulty is required")
        Difficulty difficulty,

        @Size(max = 255, message = "Thumbnail URL cannot exceed 255 characters")
        String thumbnailUrl,

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotEmpty(message = "At least one destination is required")
        Set<Long> destinationIds,

        @Valid
        @Size(max = 10, message = "A tour cannot have more than 10 images")
        List<TourImageRequest> images
) {
}
