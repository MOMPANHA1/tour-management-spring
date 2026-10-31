package co.panha.hibernate.tourmanagement.features.payment;

import co.panha.hibernate.tourmanagement.features.booking.Booking;
import co.panha.hibernate.tourmanagement.features.payment.dto.CreatePaymentRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.CreateRefundRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.PaymentResponse;
import co.panha.hibernate.tourmanagement.features.payment.dto.PaymentSummaryResponse;
import co.panha.hibernate.tourmanagement.features.payment.dto.RejectPaymentRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.RevenueReportResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

// F8 payment service.
public interface PaymentService {

    // UC8.1 customer pays.
    PaymentResponse pay(String bookingCode, CreatePaymentRequest request);

    // UC8.2 booking payment summary (owner or ADMIN).
    PaymentSummaryResponse getSummary(String bookingCode);

    // UC8.3 ADMIN verifies a payment.
    PaymentResponse verify(String referenceNo);

    // UC8.4 ADMIN rejects a payment.
    PaymentResponse reject(String referenceNo, RejectPaymentRequest request);

    // UC8.5 ADMIN creates a refund.
    PaymentResponse createRefund(String bookingCode, CreateRefundRequest request);

    // UC8.6 revenue report.
    RevenueReportResponse getRevenueReport(LocalDate fromDate, LocalDate toDate);

    // Refunds up to the paid amount for other features (F5/F7); null = full; ZERO if nothing to refund.
    BigDecimal refundForBooking(Booking booking, BigDecimal amount, String reason);
}
