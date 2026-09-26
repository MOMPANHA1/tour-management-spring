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

/**
 * សេវាកម្មទូទាត់ — F8។
 */
public interface PaymentService {

    /** UC8.1 — អតិថិជនបង់ប្រាក់។ */
    PaymentResponse pay(String bookingCode, CreatePaymentRequest request);

    /** UC8.2 — សង្ខេបទឹកប្រាក់នៃការកក់ (ម្ចាស់ ឬ ADMIN)។ */
    PaymentSummaryResponse getSummary(String bookingCode);

    /** UC8.3 — ADMIN បញ្ជាក់ការទូទាត់។ */
    PaymentResponse verify(String referenceNo);

    /** UC8.4 — ADMIN បដិសេធការទូទាត់។ */
    PaymentResponse reject(String referenceNo, RejectPaymentRequest request);

    /** UC8.5 — ADMIN បង្កើតការសងប្រាក់វិញ។ */
    PaymentResponse createRefund(String bookingCode, CreateRefundRequest request);

    /** UC8.6 — របាយការណ៍ចំណូល។ */
    RevenueReportResponse getRevenueReport(LocalDate fromDate, LocalDate toDate);

    /**
     * សងវិញពេញចំនួនដែលបានបង់ — ហៅដោយ feature ផ្សេង មិនមែនដោយ controller ទេ។
     *
     * <p>ប្រើពេលបោះបង់កាលវិភាគ (F5) ឬលុបចោលការកក់ (F7) ដែលត្រូវសងតាមវិន័យ។
     *
     * @param amount ចំនួនដែលត្រូវសង — {@code null} មានន័យថាសងពេញអ្វីដែលបានបង់
     * @return ចំនួនដែលបានបង្កើតការសងវិញពិត ({@code ZERO} បើគ្មានអ្វីត្រូវសង)
     */
    BigDecimal refundForBooking(Booking booking, BigDecimal amount, String reason);
}
