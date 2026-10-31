package co.panha.hibernate.tourmanagement.features.payment.dto;

import co.panha.hibernate.tourmanagement.features.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

// UC8.6 revenue report; counts only VERIFIED payments.
public record RevenueReportResponse(
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal totalRevenue,
        BigDecimal totalRefunded,
        BigDecimal netRevenue,
        long paymentCount,
        Map<PaymentMethod, BigDecimal> byMethod
) {
}
