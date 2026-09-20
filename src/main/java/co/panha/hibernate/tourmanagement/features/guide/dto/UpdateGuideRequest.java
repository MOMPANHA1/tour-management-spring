package co.panha.hibernate.tourmanagement.features.guide.dto;

import co.panha.hibernate.tourmanagement.base.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * ទិន្នន័យកែមគ្គុទ្ទេសក៍ — UC3.4 (PATCH)។ គ្រប់ field អាច {@code null}។
 *
 * <p>{@code status} មិនកែនៅទីនេះទេ — ប្រើ {@code PATCH /{uuid}/status} ដែលមានវិន័យអាជីវកម្មដាច់ដោយឡែក។
 */
public record UpdateGuideRequest(

        @Size(max = 120, message = "Full name cannot exceed 120 characters")
        String fullName,

        Gender gender,

        @Pattern(regexp = "^0[1-9][0-9]{7,8}$",
                message = "Phone number must start with 0 and contain 9 to 10 digits")
        String phoneNumber,

        @Email(message = "Email format is invalid")
        @Size(max = 120, message = "Email cannot exceed 120 characters")
        String email,

        Set<String> languages,

        @Min(value = 0, message = "Years of experience cannot be negative")
        @Max(value = 60, message = "Years of experience cannot exceed 60")
        Integer yearsExperience,

        @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
        String bio,

        @Size(max = 255, message = "Photo URL cannot exceed 255 characters")
        String photoUrl
) {
}
