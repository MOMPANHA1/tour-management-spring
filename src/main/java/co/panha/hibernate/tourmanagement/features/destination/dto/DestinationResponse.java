package co.panha.hibernate.tourmanagement.features.destination.dto;

import java.math.BigDecimal;

// Destination data returned to the client; tourCount is the number of active tours.
public record DestinationResponse(
        Long id,
        String name,
        String province,
        String country,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        String imageUrl,
        long tourCount
) {
}
