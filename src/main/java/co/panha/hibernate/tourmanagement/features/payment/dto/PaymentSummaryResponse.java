package co.panha.hibernate.tourmanagement.features.payment.dto;

import java.math.BigDecimal;
import java.util.List;

// UC8.2 payment summary; remaining = totalPrice - (verified - refunded).
public record PaymentSummaryResponse(
        String bookingCode,
        BigDecimal totalPrice,
        BigDecimal verifiedAmount,
        BigDecimal pendingAmount,
        BigDecimal refundedAmount,
        BigDecimal remainingAmount,
        boolean isFullyPaid,
        List<PaymentResponse> payments
) {
}
