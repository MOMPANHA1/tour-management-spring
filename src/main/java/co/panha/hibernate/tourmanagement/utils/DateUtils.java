package co.panha.hibernate.tourmanagement.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

// Date calculations: days between, age, overlap.
public final class DateUtils {

    private DateUtils() {
        // Utility class; no instances.
    }

    // Days from 'from' to 'to'; negative if 'to' is earlier (used by cancellation and refund rules).
    public static long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    // Full years of age.
    public static int yearsBetween(LocalDate from, LocalDate to) {
        return Period.between(from, to).getYears();
    }

    // True if the date is before today.
    public static boolean isPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    // True if the date is today or later.
    public static boolean isFutureOrToday(LocalDate date) {
        return !isPast(date);
    }

    // True if two date ranges overlap, end dates inclusive (startA <= endB and endA >= startB).
    public static boolean overlaps(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return !startA.isAfter(endB) && !endA.isBefore(startB);
    }
}
