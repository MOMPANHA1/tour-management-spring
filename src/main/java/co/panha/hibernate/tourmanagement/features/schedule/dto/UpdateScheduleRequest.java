package co.panha.hibernate.tourmanagement.features.schedule.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ទិន្នន័យកែកាលវិភាគ — UC5.6 (PATCH)។ គ្រប់ field អាច {@code null}។
 *
 * <p>{@code tourUuid} មិនកែបានទេ — កាលវិភាគជារបស់ Tour តែមួយជានិច្ច។ ការប្តូរ Tour
 * នឹងធ្វើឲ្យការកក់ដែលមានស្រាប់សំដៅលើដំណើរខុស។ បើត្រូវការ សូមបោះបង់ហើយបង្កើតថ្មី។
 *
 * <p>{@code guideUuid} ក៏មិនកែទីនេះដែរ — ប្រើ {@code PATCH /{uuid}/guide} ដែលមាន
 * ការពិនិត្យការជាន់គ្នា។
 */
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
