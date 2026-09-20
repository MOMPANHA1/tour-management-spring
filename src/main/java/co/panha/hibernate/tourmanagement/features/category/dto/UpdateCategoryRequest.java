package co.panha.hibernate.tourmanagement.features.category.dto;

import jakarta.validation.constraints.Size;

/**
 * ទិន្នន័យកែប្រភេទ — UC1.4 (PATCH)។
 *
 * <p>គ្រប់ field អាច {@code null} — មានន័យថា "កុំប៉ះ field នេះ"។ ដូច្នេះគ្មាន {@code @NotBlank} ទេ
 * តែ {@code @Size} នៅតែអនុវត្តលើតម្លៃដែលផ្ញើមក។
 */
public record UpdateCategoryRequest(

        @Size(max = 80, message = "Category name cannot exceed 80 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @Size(max = 255, message = "Icon URL cannot exceed 255 characters")
        String iconUrl
) {
}
