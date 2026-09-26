package co.panha.hibernate.tourmanagement.features.booking.dto;

import co.panha.hibernate.tourmanagement.features.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ទិន្នន័យការកក់សម្រាប់បញ្ជី។
 *
 * @param customerName ឈ្មោះអតិថិជន — <b>{@code null} សម្រាប់អតិថិជនខ្លួនឯង</b>
 *                     ហើយបំពេញតែពេល ADMIN មើល។ គ្មានប្រយោជន៍បង្ហាញឈ្មោះខ្លួនឯងឡើងវិញ
 *                     ក្នុងបញ្ជីការកក់របស់ខ្លួន។
 * @param remainingAmount {@code totalPrice − paidAmount} — គណនាពេលអាន មិនរក្សាទុក។
 */
public record BookingResponse(
        String code,
        BookingStatus status,
        Long tourId,
        String tourTitle,
        Long scheduleId,
        LocalDate departureDate,
        LocalDate returnDate,
        String meetingPoint,
        Integer numberOfPeople,
        BigDecimal unitPrice,
        BigDecimal subTotal,
        BigDecimal discountAmount,
        BigDecimal totalPrice,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        LocalDateTime bookedAt,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt,
        String customerName
) {
}
