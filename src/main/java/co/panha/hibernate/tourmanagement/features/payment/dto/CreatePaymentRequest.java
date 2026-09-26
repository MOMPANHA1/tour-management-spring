package co.panha.hibernate.tourmanagement.features.payment.dto;

import co.panha.hibernate.tourmanagement.features.payment.PaymentMethod;
import co.panha.hibernate.tourmanagement.features.payment.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * សំណើទូទាត់ — UC8.1។
 *
 * <p>{@code type} មិនអាចជា {@code REFUND} ទេ — ការសងប្រាក់វិញប្រើ endpoint ដាច់ដោយឡែក
 * ដែលតម្រូវឲ្យមានសិទ្ធិ ADMIN។
 */
public record CreatePaymentRequest(

        @NotNull(message = "Payment type is required")
        PaymentType type,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @Size(max = 120, message = "Transaction id cannot exceed 120 characters")
        String transactionId,

        @Size(max = 255, message = "Receipt URL cannot exceed 255 characters")
        String receiptUrl,

        @Size(max = 500, message = "Note cannot exceed 500 characters")
        String note
) {
}
