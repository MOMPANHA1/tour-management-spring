package co.panha.hibernate.tourmanagement.features.customer.dto;

import co.panha.hibernate.tourmanagement.base.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * ទិន្នន័យចុះឈ្មោះអតិថិជនថ្មី — UC6.1។
 *
 * <p><b>ខុសពីផែនការដើម</b>៖ បន្ថែម {@code password} និង {@code confirmedPassword} ដែល
 * {@code pseudo-code.md} មិនបានរាយ។ ចាំបាច់ព្រោះការចុះឈ្មោះបង្កើត user ក្នុង Keycloak ផង —
 * គ្មានពាក្យសម្ងាត់ទេ អតិថិជននឹងចុះឈ្មោះបានតែ login មិនបាន។
 *
 * <p>ពាក្យសម្ងាត់ <b>មិនរក្សាទុកក្នុង database</b> ទេ — ផ្ញើទៅ Keycloak ហើយបោះចោល។
 */
public record RegisterCustomerRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 60, message = "Username must be between 4 and 60 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username may contain only letters, digits, dot, underscore and hyphen")
        String username,

        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name cannot exceed 120 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 120, message = "Email cannot exceed 120 characters")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^0[1-9][0-9]{7,8}$",
                message = "Phone number must be a valid Cambodian number, e.g. 012345678")
        String phoneNumber,

        Gender gender,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 80, message = "Nationality cannot exceed 80 characters")
        String nationality,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "Confirmed password is required")
        String confirmedPassword
) {
}
