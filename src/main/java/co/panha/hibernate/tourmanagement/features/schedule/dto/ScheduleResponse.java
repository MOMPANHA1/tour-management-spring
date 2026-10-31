package co.panha.hibernate.tourmanagement.features.schedule.dto;

import co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// bookedSeats is currently always 0 (no Booking yet); availableSeats is computed on read; effectivePrice falls back to tour.price.
public record ScheduleResponse(
        Long id,
        String code,
        Long tourId,
        String tourTitle,
        Long guideId,
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
