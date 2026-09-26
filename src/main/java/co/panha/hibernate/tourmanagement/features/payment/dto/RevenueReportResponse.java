package co.panha.hibernate.tourmanagement.features.payment.dto;

import co.panha.hibernate.tourmanagement.features.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * របាយការណ៍ចំណូល — UC8.6។
 *
 * <p>រាប់តែការទូទាត់ស្ថានភាព {@code VERIFIED} ប៉ុណ្ណោះ — ការផ្ទេរដែលមិនទាន់ពិនិត្យ
 * មិនមែនជាចំណូលពិតទេ។
 */
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
