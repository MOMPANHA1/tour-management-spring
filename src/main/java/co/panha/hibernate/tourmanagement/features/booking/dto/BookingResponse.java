package co.panha.hibernate.tourmanagement.features.booking.dto;

import co.panha.hibernate.tourmanagement.features.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// List view; customerName is null for the customer's own view, remainingAmount is computed on read.
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
