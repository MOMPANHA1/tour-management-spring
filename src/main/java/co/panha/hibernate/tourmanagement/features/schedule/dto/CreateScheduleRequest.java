package co.panha.hibernate.tourmanagement.features.schedule.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ទិន្នន័យបង្កើតកាលវិភាគ — UC5.1។
 *
 * <p>រយៈពេល ({@code returnDate − departureDate + 1}) ត្រូវស្មើ {@code tour.durationDays}
 * — ពិនិត្យក្នុង Service ព្រោះវាត្រូវអាន Tour ពី database។
 */
public record CreateScheduleRequest(

        @NotNull(message = "Tour is required")
        Long tourId,

        /** ជម្រើស — ចាត់តាំងមគ្គុទ្ទេសក៍ក្រោយបាន។ */
        Long guideId,

        @NotNull(message = "Departure date is required")
        @FutureOrPresent(message = "Departure date cannot be in the past")
        LocalDate departureDate,

        @NotNull(message = "Return date is required")
        @Future(message = "Return date must be in the future")
        LocalDate returnDate,

        LocalTime departureTime,

        @Size(max = 255, message = "Meeting point cannot exceed 255 characters")
        String meetingPoint,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 200, message = "Capacity cannot exceed 200")
        Integer capacity,

        @DecimalMin(value = "0.01", message = "Price override must be greater than 0")
        BigDecimal priceOverride
) {
}
