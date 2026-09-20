package co.panha.hibernate.tourmanagement.features.tour.dto;

public record TourImageResponse(
        String url,
        String caption,
        Integer sortOrder
) {
}
