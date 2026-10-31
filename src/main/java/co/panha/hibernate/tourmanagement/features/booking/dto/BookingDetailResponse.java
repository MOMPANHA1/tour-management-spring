package co.panha.hibernate.tourmanagement.features.booking.dto;

import co.panha.hibernate.tourmanagement.features.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Full booking detail; fields repeated from BookingResponse since records cannot extend. TODO: add payments in F8.
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
