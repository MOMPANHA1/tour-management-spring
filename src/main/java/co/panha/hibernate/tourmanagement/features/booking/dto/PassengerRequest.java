package co.panha.hibernate.tourmanagement.features.booking.dto;

import co.panha.hibernate.tourmanagement.base.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** អ្នកដំណើរម្នាក់ក្នុងសំណើកក់។ */
public record PassengerRequest(

        @NotBlank(message = "Passenger full name is required")
        @Size(max = 120, message = "Passenger full name cannot exceed 120 characters")
        String fullName,

        @NotNull(message = "Passenger gender is required")
        Gender gender,

        @Past(message = "Passenger date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 40, message = "Passport number cannot exceed 40 characters")
        String passportNo,

        /** ទុក {@code null} បាន — Service ចាត់ទុកជា {@code false}។ */
        Boolean isLead
) {
}
