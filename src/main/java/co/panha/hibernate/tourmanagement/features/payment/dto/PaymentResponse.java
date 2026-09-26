package co.panha.hibernate.tourmanagement.features.payment.dto;

import co.panha.hibernate.tourmanagement.features.payment.PaymentMethod;
import co.panha.hibernate.tourmanagement.features.payment.PaymentStatus;
import co.panha.hibernate.tourmanagement.features.payment.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String referenceNo,
        String bookingCode,
        PaymentType type,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String transactionId,
        String receiptUrl,
        String note,
        String rejectReason,
        LocalDateTime paidAt,
        LocalDateTime verifiedAt,
        String verifiedBy
) {
}
