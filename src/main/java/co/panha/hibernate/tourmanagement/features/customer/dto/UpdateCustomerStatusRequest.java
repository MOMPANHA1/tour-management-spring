package co.panha.hibernate.tourmanagement.features.customer.dto;

import co.panha.hibernate.tourmanagement.features.customer.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ការប្តូរស្ថានភាពគណនីដោយ ADMIN — UC6.6។
 *
 * <p>{@code reason} ជាជម្រើសទូទៅ តែ<b>ចាំបាច់</b>នៅពេលផ្អាកអតិថិជនដែលមានការកក់សកម្ម។
 */
public record UpdateCustomerStatusRequest(

        @NotNull(message = "Status is required")
        CustomerStatus status,

        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        String reason
) {
}
