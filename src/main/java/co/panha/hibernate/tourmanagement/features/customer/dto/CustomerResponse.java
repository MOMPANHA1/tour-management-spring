package co.panha.hibernate.tourmanagement.features.customer.dto;

import co.panha.hibernate.tourmanagement.base.Gender;
import co.panha.hibernate.tourmanagement.features.customer.CustomerStatus;

import java.time.LocalDate;

/**
 * ទិន្នន័យអតិថិជនដែលត្រឡប់ទៅ client។
 *
 * <p><b>មិនផ្ទុក {@code passportNo} និង {@code keycloakId}</b> ដោយចេតនា — មួយរសើប
 * មួយទៀតជាព័ត៌មានខាងក្នុងប្រព័ន្ធ។
 *
 * @param totalBookings  ចំនួនការកក់សរុប។ <b>បច្ចុប្បន្នតែងតែ 0</b> ព្រោះ entity
 *                       {@code Booking} មិនទាន់មាន (F7)។
 * @param completedTours ចំនួនដំណើរដែលបញ្ចប់។ <b>បច្ចុប្បន្នតែងតែ 0</b> — ដូចគ្នា។
 * @param memberSince    ថ្ងៃចុះឈ្មោះ — ទាញពី {@code createdAt}។
 */
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
