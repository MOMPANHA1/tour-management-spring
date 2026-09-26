package co.panha.hibernate.tourmanagement.features.customer.dto;

import co.panha.hibernate.tourmanagement.base.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * ទិន្នន័យកែប្រវត្តិរូបខ្លួនឯង — UC6.3 (PATCH)។
 *
 * <p>គ្រប់ field អាច {@code null} — មានន័យថា "កុំប៉ះ field នេះ"។
 *
 * <p><b>{@code username} និង {@code email} កែមិនបានទេ</b> — ព្រោះវាជាអត្តសញ្ញាណក្នុង Keycloak។
 * ការប្តូរវាត្រូវធ្វើនៅ Keycloak ដោយផ្ទាល់ បើមិនដូច្នេះទេ ទិន្នន័យ ២ កន្លែងនឹងខុសគ្នា។
 */
public record PatchCustomerRequest(

        @Size(max = 120, message = "Full name cannot exceed 120 characters")
        String fullName,

        @Pattern(regexp = "^0[1-9][0-9]{7,8}$",
                message = "Phone number must be a valid Cambodian number, e.g. 012345678")
        String phoneNumber,

        Gender gender,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 80, message = "Nationality cannot exceed 80 characters")
        String nationality,

        @Size(max = 40, message = "Passport number cannot exceed 40 characters")
        String passportNo,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @Size(max = 255, message = "Avatar URL cannot exceed 255 characters")
        String avatarUrl
) {
}
