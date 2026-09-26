package co.panha.hibernate.tourmanagement.features.review.dto;

import java.util.Map;

/**
 * សង្ខេបពិន្ទុរបស់ Tour មួយ — UC9.7។
 *
 * @param starCounts      ចំនួនតាមផ្កាយ — មានគ្រប់ ១..៥ ជានិច្ច (0 បើគ្មាន)
 * @param starPercentages ភាគរយតាមផ្កាយ — សម្រាប់គូររបារលើអេក្រង់
 */
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
