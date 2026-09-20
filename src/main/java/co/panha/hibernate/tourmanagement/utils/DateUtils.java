package co.panha.hibernate.tourmanagement.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * ការគណនាកាលបរិច្ឆេទ — ចំនួនថ្ងៃ, អាយុ, ការត្រួតគ្នានៃចន្លោះពេល។
 */
public final class DateUtils {

    private DateUtils() {
        // ថ្នាក់ឧបករណ៍ — មិនបង្កើត instance
    }

    /**
     * ចំនួនថ្ងៃពី {@code from} ដល់ {@code to} (អាចអវិជ្ជមាន បើ {@code to} នៅមុន)។
     *
     * <p>ប្រើក្នុង {@code BR8} (លុបចោលយ៉ាងតិច ៣ ថ្ងៃមុនចេញដំណើរ) និង {@code calculateRefundRate}។
     */
    public static long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    /** អាយុជាឆ្នាំពេញ — ប្រើសម្រាប់គណនាអាយុអ្នកដំណើរពី {@code dateOfBirth}។ */
    public static int yearsBetween(LocalDate from, LocalDate to) {
        return Period.between(from, to).getYears();
    }

    /** កាលបរិច្ឆេទនេះកន្លងផុតហើយឬនៅ (ថ្ងៃនេះមិនរាប់ជាអតីតកាល)។ */
    public static boolean isPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    /** កាលបរិច្ឆេទនេះនៅអនាគតឬនៅថ្ងៃនេះ។ */
    public static boolean isFutureOrToday(LocalDate date) {
        return !isPast(date);
    }

    /**
     * ចន្លោះពេល ២ ត្រួតគ្នាឬអត់ (រាប់បញ្ចូលថ្ងៃចុងបញ្ចប់)។
     *
     * <p>ប្រើក្នុង {@code hasGuideConflict} — មគ្គុទ្ទេសក៍ម្នាក់មិនអាចនាំដំណើរ ២ ព្រមគ្នា។
     *
     * <p>ការត្រួតគ្នាកើតឡើងនៅពេល {@code startA <= endB} និង {@code endA >= startB}។
     * ឧទាហរណ៍៖ ១០–១៥ តុលា ត្រួតនឹង ១២–១៤ តុលា និងនឹង ១៥–២០ តុលា (ថ្ងៃទី ១៥ ជាប់គ្នា)។
     */
    public static boolean overlaps(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return !startA.isAfter(endB) && !endA.isBefore(startB);
    }
}
