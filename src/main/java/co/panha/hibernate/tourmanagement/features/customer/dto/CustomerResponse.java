package co.panha.hibernate.tourmanagement.features.customer.dto;

import co.panha.hibernate.tourmanagement.base.Gender;
import co.panha.hibernate.tourmanagement.features.customer.CustomerStatus;

import java.time.LocalDate;

// Excludes passportNo (sensitive) and keycloakId (internal) on purpose; memberSince comes from createdAt.
public record CustomerResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phoneNumber,
        Gender gender,
        LocalDate dateOfBirth,
        String nationality,
        String address,
        String avatarUrl,
        CustomerStatus status,
        long totalBookings,
        long completedTours,
        LocalDate memberSince
) {
}
