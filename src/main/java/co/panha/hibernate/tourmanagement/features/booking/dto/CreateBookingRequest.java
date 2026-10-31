package co.panha.hibernate.tourmanagement.features.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// UC7.1 request; no customerId here, the service reads it from the JWT so nobody can book for others.
public record CreateBookingRequest(

        @NotNull(message = "Schedule id is required")
        Long scheduleId,

        @NotNull(message = "Number of people is required")
        @Min(value = 1, message = "Number of people must be at least 1")
        @Max(value = 20, message = "Number of people cannot exceed 20")
        Integer numberOfPeople,

        @Size(max = 500, message = "Note cannot exceed 500 characters")
        String note,

        @NotEmpty(message = "At least one passenger is required")
        @Valid
        List<PassengerRequest> passengers
) {
}
