package co.panha.hibernate.tourmanagement.features.payment.dto;

import co.panha.hibernate.tourmanagement.features.payment.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** សំណើសងប្រាក់វិញដោយ ADMIN — UC8.5។ */
public record CreateRefundRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Refund reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason,

        @NotNull(message = "Refund method is required")
        PaymentMethod method
) {
}
