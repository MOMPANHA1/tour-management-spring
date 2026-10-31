package co.panha.hibernate.tourmanagement.features.category.dto;

// Category data returned to the client; tourCount is the number of active tours.
public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String iconUrl,
        long tourCount
) {
}
