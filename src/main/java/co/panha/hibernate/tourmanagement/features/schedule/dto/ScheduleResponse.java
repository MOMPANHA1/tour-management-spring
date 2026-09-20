package co.panha.hibernate.tourmanagement.features.schedule.dto;

import co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ទិន្នន័យកាលវិភាគដែលត្រឡប់ទៅ client។
 *
 * @param bookedSeats    កៅអីដែលកក់រួច។ <b>បច្ចុប្បន្នតែងតែ 0</b> ព្រោះ entity {@code Booking}
 *                       មិនទាន់មាន (F7)។
 * @param availableSeats {@code capacity − bookedSeats} — គណនាពេលអាន មិនរក្សាទុកក្នុង database
 *                       ព្រោះវាប្រែរាល់ការកក់។
 * @param effectivePrice {@code priceOverride} បើមាន បើមិនដូច្នេះ {@code tour.price}។
 */
public record ScheduleResponse(
        String uuid,
        String code,
        String tourUuid,
        String tourTitle,
        String guideUuid,
        String guideName,
        LocalDate departureDate,
        LocalDate returnDate,
        LocalTime departureTime,
        String meetingPoint,
        Integer capacity,
        int bookedSeats,
        int availableSeats,
        BigDecimal effectivePrice,
        ScheduleStatus status
) {
}
