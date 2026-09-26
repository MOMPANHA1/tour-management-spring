package co.panha.hibernate.tourmanagement.features.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * កែចំនួន និងបញ្ជីអ្នកដំណើរ — UC7.7។
 *
 * <p>បញ្ជីជាការ<b>ជំនួសពេញ</b> មិនមែនការបន្ថែមទេ — អ្នកដំណើរចាស់ត្រូវលុបចោលទាំងអស់។
 */
public record UpdatePassengersRequest(

        @NotNull(message = "Number of people is required")
        @Min(value = 1, message = "Number of people must be at least 1")
        @Max(value = 20, message = "Number of people cannot exceed 20")
        Integer numberOfPeople,

        @NotEmpty(message = "At least one passenger is required")
        @Valid
        List<PassengerRequest> passengers
) {
}
