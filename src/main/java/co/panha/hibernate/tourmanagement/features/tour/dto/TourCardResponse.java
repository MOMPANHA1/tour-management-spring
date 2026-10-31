package co.panha.hibernate.tourmanagement.features.tour.dto;

import co.panha.hibernate.tourmanagement.features.tour.Difficulty;

import java.math.BigDecimal;
import java.time.LocalDate;

// Light shape for tour lists (UC4.2, UC4.7); nextDepartureDate is null when no open schedule exists.
public record TourCardResponse(
        Long id,
        String code,
        String title,
        String slug,
        String thumbnailUrl,
        BigDecimal price,
        Integer durationDays,
        Integer durationNights,
        Difficulty difficulty,
        String categoryName,
        Double averageRating,
        Integer reviewCount,
        LocalDate nextDepartureDate
) {
}
