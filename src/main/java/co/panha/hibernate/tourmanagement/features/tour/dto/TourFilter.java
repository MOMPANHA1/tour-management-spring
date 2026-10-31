package co.panha.hibernate.tourmanagement.features.tour.dto;

import co.panha.hibernate.tourmanagement.features.tour.Difficulty;

import java.math.BigDecimal;
import java.time.LocalDate;

// UC4.2 filter, all optional; unknown sortBy falls back to newest; departureAfter is not implemented yet.
public record TourFilter(
        String keyword,
        Long categoryId,
        Long destinationId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minDays,
        Integer maxDays,
        Difficulty difficulty,
        LocalDate departureAfter,
        String sortBy
) {
}
