package co.panha.hibernate.tourmanagement.features.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ទិន្នន័យបង្កើតប្រភេទថ្មី — UC1.1។
 *
 * <p>{@code slug} មិនទទួលពី client ទេ — ប្រព័ន្ធបង្កើតពី {@code name}។
 */
public record CreateCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 80, message = "Category name cannot exceed 80 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @Size(max = 255, message = "Icon URL cannot exceed 255 characters")
        String iconUrl
) {
}
