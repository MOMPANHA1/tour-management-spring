package co.panha.hibernate.tourmanagement.features.payment.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * សេចក្តីសង្ខេបទឹកប្រាក់នៃការកក់មួយ — UC8.2។
 *
 * @param verifiedAmount  ចំណូលដែលបញ្ជាក់រួច (មិនរាប់ការសងវិញ)
 * @param pendingAmount   ចំនួនដែលរង់ចាំ ADMIN ពិនិត្យ
 * @param refundedAmount  ចំនួនដែលបានសងវិញ ឬកំពុងសង
 * @param remainingAmount {@code totalPrice − (verified − refunded)}
 */
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
