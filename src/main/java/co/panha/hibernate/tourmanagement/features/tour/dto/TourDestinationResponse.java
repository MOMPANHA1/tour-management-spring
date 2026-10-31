package co.panha.hibernate.tourmanagement.features.tour.dto;

import java.math.BigDecimal;

// Destination in tour detail; avoids DestinationResponse because its tourCount is meaningless here.
public record TourDestinationResponse(
        Long id,
        String name,
        String province,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        String imageUrl
) {
}
