package co.panha.hibernate.tourmanagement.features.tour.dto;

import co.panha.hibernate.tourmanagement.features.tour.Difficulty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * លក្ខខណ្ឌស្វែងរក Tour — UC4.2។ គ្រប់ field ជាជម្រើស។
 *
 * @param sortBy         {@code price_asc} · {@code price_desc} · {@code rating} · {@code newest}
 *                       (លំនាំដើម)។ តម្លៃផ្សេងធ្លាក់ទៅលំនាំដើម — សូមមើល {@code TourServiceImpl}។
 * @param departureAfter <b>មិនទាន់ដំណើរការ</b> — ត្រូវការ {@code TourSchedule} (F5)។
 */
public record TourFilter(
        String keyword,
        String categoryUuid,
        String destinationUuid,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minDays,
        Integer maxDays,
        Difficulty difficulty,
        LocalDate departureAfter,
        String sortBy
) {
}
