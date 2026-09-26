package co.panha.hibernate.tourmanagement.features.payment;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import co.panha.hibernate.tourmanagement.features.booking.Booking;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ការទូទាត់មួយចលនា — F8។
 *
 * <p><b>តារាងនេះជាសៀវភៅកត់ត្រា (ledger) មិនមែនជាស្ថានភាពទេ</b>៖ ជួរនីមួយៗជាព្រឹត្តិការណ៍
 * ដែលកើតឡើងហើយ ហើយ<b>មិនត្រូវកែ</b>បន្ទាប់ពីបញ្ជាក់។ ចំនួនប្រាក់សរុបគណនាដោយបូកជួរ
 * មិនមែនដោយរក្សាទុកលេខតែមួយទេ — {@code booking.paidAmount} គ្រាន់តែជាតម្លៃគណនាទុកមុន
 * ដែលធ្វើសមកាលកម្មពីតារាងនេះ ({@code syncBookingPaidAmount})។
 *
 * <p><b>គ្មានការតភ្ជាប់ទៅ payment gateway</b> — {@code transactionId} ជាលេខយោងដែល
 * អតិថិជនវាយដាក់ ឬចម្លងពីកម្មវិធីធនាគារ។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_booking_status", columnList = "booking_id, status"),
                @Index(name = "idx_payment_paid_at", columnList = "paid_at"),
                @Index(name = "idx_payment_transaction", columnList = "transaction_id")
        }
)
public class Payment extends BaseEntity {

    /** លេខយោងសាធារណៈ — {@code PM-20260912-0001}។ ប្រើជាកូនសោលើ API ជំនួស {@code id}។ */
    @Column(unique = true, nullable = false, length = 40)
    private String referenceNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    /** តែងតែជាចំនួន<b>វិជ្ជមាន</b> — ទិសកំណត់ដោយ {@link #type} មិនមែនដោយសញ្ញាទេ។ */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /** លេខយោងពីធនាគារ ឬ gateway — unique ដើម្បីការពារការកត់ត្រាដដែលពីរដង។ */
    @Column(length = 120)
    private String transactionId;

    /** URL រូបភាពវិក្កយបត្រ — ចាំបាច់សម្រាប់ {@code BANK_TRANSFER} (BR5)។ */
    @Column(length = 255)
    private String receiptUrl;

    @Column(length = 500)
    private String note;

    @Column(length = 500)
    private String rejectReason;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    private LocalDateTime verifiedAt;

    /** អ្នកបញ្ជាក់ — {@code username} របស់ ADMIN ឬ {@code "SYSTEM"} សម្រាប់ការបង់អនឡាញ។ */
    @Column(length = 60)
    private String verifiedBy;

    /** ការទូទាត់នេះរាប់ចូល {@code paidAmount} ឬអត់។ */
    public boolean isIncome() {
        return type != PaymentType.REFUND;
    }
}
