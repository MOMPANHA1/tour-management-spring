package co.panha.hibernate.tourmanagement.features.tour.dto;

import co.panha.hibernate.tourmanagement.features.tour.Difficulty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * រូបរាងពេញលេញសម្រាប់ទំព័រលម្អិត Tour (UC4.3)។
 *
 * <p>Record មិនអាច {@code extends} បានទេ ដូច្នេះ field របស់ {@link TourCardResponse}
 * ត្រូវសរសេរម្តងទៀតនៅទីនេះ — នេះជាការដោះដូរដែលទទួលយកបាន ព្រោះ record ផ្តល់
 * ភាពមិនប្រែប្រួល និង {@code equals}/{@code hashCode} ដោយឥតគិតថ្លៃ។
 *
 * <p>TODO ដំណាក់កាល ៣៖ បន្ថែម {@code upcomingSchedules} (F5) និង {@code recentReviews} (F9)។
 */
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
