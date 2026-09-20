package co.panha.hibernate.tourmanagement.features.tour.dto;

import co.panha.hibernate.tourmanagement.features.tour.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * ទិន្នន័យកែ Tour — UC4.4 (PATCH)។ គ្រប់ field អាច {@code null}។
 *
 * <p><b>ចំណាំអំពី {@code images} និង {@code destinationUuids}</b>៖ ពួកវាជា <i>បញ្ជីពេញ</i>
 * មិនមែនការបន្ថែមទេ។ ការផ្ញើ {@code images: []} នឹងលុបរូបភាពទាំងអស់ ចំណែកការមិនផ្ញើ
 * ({@code null}) នឹងទុកបញ្ជីចាស់ដដែល។
 */
public record UpdateTourRequest(

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

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @Min(value = 1, message = "Duration must be at least 1 day")
        @Max(value = 90, message = "Duration cannot exceed 90 days")
        Integer durationDays,

        @Min(value = 0, message = "Nights cannot be negative")
        @Max(value = 90, message = "Nights cannot exceed 90")
        Integer durationNights,

        @Min(value = 1, message = "Minimum group size must be at least 1")
        Integer minGroupSize,

        @Min(value = 1, message = "Maximum group size must be at least 1")
        @Max(value = 200, message = "Maximum group size cannot exceed 200")
        Integer maxGroupSize,

        Difficulty difficulty,

        @Size(max = 255, message = "Thumbnail URL cannot exceed 255 characters")
        String thumbnailUrl,

        String categoryUuid,

        Set<String> destinationUuids,

        @Valid
        @Size(max = 10, message = "A tour cannot have more than 10 images")
        List<TourImageRequest> images
) {
}
