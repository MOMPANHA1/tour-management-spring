package co.panha.hibernate.tourmanagement.features.destination.dto;

import java.math.BigDecimal;

/**
 * ទិន្នន័យទីតាំងដែលត្រឡប់ទៅ client។
 *
 * @param tourCount ចំនួន Tour សកម្មដែលទៅដល់ទីតាំងនេះ។
 */
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
