package co.panha.hibernate.tourmanagement.features.destination.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * ទិន្នន័យបង្កើតទីតាំងថ្មី — UC2.1។
 *
 * <p>{@code country} ជាជម្រើស — បើទុកទទេប្រព័ន្ធដាក់ {@code "Cambodia"}។
 */
public record CreateDestinationRequest(

        @NotBlank(message = "Destination name is required")
        @Size(max = 120, message = "Destination name cannot exceed 120 characters")
        String name,

        @NotBlank(message = "Province is required")
        @Size(max = 80, message = "Province cannot exceed 80 characters")
        String province,

        @Size(max = 80, message = "Country cannot exceed 80 characters")
        String country,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
        BigDecimal longitude,

        @Size(max = 255, message = "Image URL cannot exceed 255 characters")
        String imageUrl
) {
}
