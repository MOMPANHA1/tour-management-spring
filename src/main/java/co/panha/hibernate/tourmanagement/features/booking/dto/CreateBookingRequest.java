package co.panha.hibernate.tourmanagement.features.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * សំណើកក់ — UC7.1។
 *
 * <p><b>ខុសពីផែនការដើម</b>៖ {@code scheduleId} ជា {@code Long} មិនមែន {@code String} ទេ
 * (ផែនការសរសេរ {@code String @NotBlank} ដែលជាកំហុសវាយអក្សរ — {@code TourSchedule.id}
 * ជា {@code Long} តាំងពី {@code BaseEntity} មក)។
 *
 * <p>{@code customerId} មិនទទួលពី client ទេ — Service ទាញពី JWT។ បើទទួល
 * នោះអ្នកណាក៏កក់ជំនួសអ្នកដទៃបាន។
 */
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
