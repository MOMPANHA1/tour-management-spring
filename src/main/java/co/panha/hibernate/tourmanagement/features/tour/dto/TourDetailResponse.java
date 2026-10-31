package co.panha.hibernate.tourmanagement.features.tour.dto;

import co.panha.hibernate.tourmanagement.features.tour.Difficulty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Full tour detail (UC4.3); records cannot extend, so card fields are repeated. TODO: add upcomingSchedules (F5), recentReviews (F9).
public record TourDetailResponse(
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
        LocalDate nextDepartureDate,

        String description,
        String itinerary,
        String included,
        String excluded,
        Integer minGroupSize,
        Integer maxGroupSize,
        Boolean isPublished,
        Long categoryId,
        List<TourDestinationResponse> destinations,
        List<TourImageResponse> images
) {
}
