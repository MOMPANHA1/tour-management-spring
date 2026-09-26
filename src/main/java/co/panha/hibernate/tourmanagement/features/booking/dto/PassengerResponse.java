package co.panha.hibernate.tourmanagement.features.booking.dto;

import co.panha.hibernate.tourmanagement.base.Gender;

import java.time.LocalDate;

public record PassengerResponse(
        Long id,
        String fullName,
        Gender gender,
        LocalDate dateOfBirth,
        String passportNo,
        Boolean isLead
) {
}
