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

// Ledger entry: rows are events and must not be edited after verification; no real gateway.
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

    // Public reference like PM-20260912-0001; used as the API key instead of id.
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

    // Always positive; direction is determined by type, not sign.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    // Bank or gateway reference; unique to prevent duplicate records.
    @Column(length = 120)
    private String transactionId;

    // Receipt image URL; required for BANK_TRANSFER (BR5).
    @Column(length = 255)
    private String receiptUrl;

    @Column(length = 500)
    private String note;

    @Column(length = 500)
    private String rejectReason;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    private LocalDateTime verifiedAt;

    // ADMIN username, or "SYSTEM" for online payments.
    @Column(length = 60)
    private String verifiedBy;

    // Whether this payment counts toward paidAmount.
    public boolean isIncome() {
        return type != PaymentType.REFUND;
    }
}
