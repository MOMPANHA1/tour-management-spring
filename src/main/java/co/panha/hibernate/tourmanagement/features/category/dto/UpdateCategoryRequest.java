package co.panha.hibernate.tourmanagement.features.category.dto;

import jakarta.validation.constraints.Size;

// UC1.4 - PATCH request; null fields are left untouched, but @Size still applies to sent values.
public record UpdateCategoryRequest(

        @Size(max = 80, message = "Category name cannot exceed 80 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @Size(max = 255, message = "Icon URL cannot exceed 255 characters")
        String iconUrl
) {
}
