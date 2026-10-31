package co.panha.hibernate.tourmanagement.features.review.dto;

import java.util.Map;

// UC9.7 - rating summary of a tour; starCounts always contains stars 1..5 (0 if none).
public record ReviewSummaryResponse(
        Long tourId,
        Double averageRating,
        long totalReviews,
        Double averageGuideRating,
        Double averageValueRating,
        Map<Integer, Long> starCounts,
        Map<Integer, Double> starPercentages
) {
}
