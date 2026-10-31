package co.panha.hibernate.tourmanagement.features.customer.dto;

import co.panha.hibernate.tourmanagement.features.customer.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// UC6.6 status change by ADMIN; reason is required when suspending a customer with active bookings.
public record UpdateCustomerStatusRequest(

        @NotNull(message = "Status is required")
        CustomerStatus status,

        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        String reason
) {
}
