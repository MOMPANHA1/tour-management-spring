package co.panha.hibernate.tourmanagement.features.schedule.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// UC5.6 PATCH: all fields nullable; tourId and guideId are not editable here (use cancel+create / PATCH /{id}/guide).
public record UpdateScheduleRequest(

        @FutureOrPresent(message = "Departure date cannot be in the past")
        LocalDate departureDate,

        LocalDate returnDate,

        LocalTime departureTime,

        @Size(max = 255, message = "Meeting point cannot exceed 255 characters")
        String meetingPoint,

        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 200, message = "Capacity cannot exceed 200")
        Integer capacity,

        @DecimalMin(value = "0.01", message = "Price override must be greater than 0")
        BigDecimal priceOverride
) {
}
