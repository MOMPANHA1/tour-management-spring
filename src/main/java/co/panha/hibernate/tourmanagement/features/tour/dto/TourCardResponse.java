package co.panha.hibernate.tourmanagement.features.tour.dto;

import co.panha.hibernate.tourmanagement.features.tour.Difficulty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * រូបរាងស្រាលសម្រាប់បញ្ជី Tour (UC4.2, UC4.7)។
 *
 * @param nextDepartureDate ថ្ងៃចេញដំណើរបន្ទាប់។ <b>បច្ចុប្បន្នតែងតែ null</b> ព្រោះ entity
 *                          {@code TourSchedule} មិនទាន់មាន (F5)។ ពេល null អេក្រង់គួរបង្ហាញ
 *                          "ឆាប់ៗនេះ" ហើយលាក់ប៊ូតុងកក់ — សូមមើល {@code ux-flow.md} §២.១។
 */
public record TourCardResponse(
        String uuid,
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
