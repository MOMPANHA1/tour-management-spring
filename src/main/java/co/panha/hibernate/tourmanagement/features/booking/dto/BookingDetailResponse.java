package co.panha.hibernate.tourmanagement.features.booking.dto;

import co.panha.hibernate.tourmanagement.features.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ទិន្នន័យការកក់ពេញលេញ។
 *
 * <p>ផែនការសរសេរ {@code BookingDetailResponse EXTENDS BookingResponse} — តែ Java record
 * ពង្រីកមិនបានទេ ដូច្នេះរាយ field ឡើងវិញ ដូចលំនាំ
 * {@code TourCardResponse} / {@code TourDetailResponse} ដែរ។
 *
 * <p><b>មិនទាន់មាន {@code payments}</b> — {@code Payment} មកនៅ F8។
 */
public record BookingDetailResponse(
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
        String customerName,
        String note,
        String cancelReason,
        String guideName,
        List<PassengerResponse> passengers
) {
}
