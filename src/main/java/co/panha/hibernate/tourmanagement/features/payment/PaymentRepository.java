package co.panha.hibernate.tourmanagement.features.payment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"booking", "booking.customer"})
    Optional<Payment> findByReferenceNoAndIsDeletedFalse(String referenceNo);

    boolean existsByReferenceNo(String referenceNo);

    boolean existsByTransactionId(String transactionId);

    List<Payment> findAllByBookingIdAndIsDeletedFalseOrderByCreatedAtDesc(Long bookingId);

    /**
     * ចំនួនចំណូលដែលបញ្ជាក់រួច — ញែក {@code REFUND} ចេញ។
     *
     * <p>{@code COALESCE} ចាំបាច់៖ {@code SUM} លើសំណុំទទេត្រឡប់ {@code null} មិនមែន 0 ទេ
     * ដែលនឹងបង្ក {@code NullPointerException} ពេលធ្វើប្រមាណវិធី។
     */
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.booking.id = :bookingId
              AND p.isDeleted = false
              AND p.status = co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.VERIFIED
              AND p.type <> co.panha.hibernate.tourmanagement.features.payment.PaymentType.REFUND
            """)
    BigDecimal sumVerifiedAmount(@Param("bookingId") Long bookingId);

    /** ចំនួនដែលកំពុងរង់ចាំការបញ្ជាក់ — រាប់ចូលដើម្បីការពារការបង់លើស។ */
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.booking.id = :bookingId
              AND p.isDeleted = false
              AND p.status = co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.PENDING
              AND p.type <> co.panha.hibernate.tourmanagement.features.payment.PaymentType.REFUND
            """)
    BigDecimal sumPendingAmount(@Param("bookingId") Long bookingId);

    /**
     * ចំនួនដែលសងវិញ។
     *
     * <p>រាប់ទាំង {@code PENDING} ផង ខុសពីការបូកចំណូល — ព្រោះការសងវិញដែលបានសម្រេច
     * ជាកាតព្វកិច្ចរួចហើយ ទោះបីហិរញ្ញវត្ថុមិនទាន់ផ្ទេរក៏ដោយ។ បើមិនរាប់ ADMIN អាចបង្កើត
     * ការសងវិញច្រើនដងលើលុយដដែល។
     */
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.booking.id = :bookingId
              AND p.isDeleted = false
              AND p.type = co.panha.hibernate.tourmanagement.features.payment.PaymentType.REFUND
              AND p.status IN (co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.VERIFIED,
                               co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.REFUNDED)
            """)
    BigDecimal sumRefundedAmount(@Param("bookingId") Long bookingId);

    // ---------- របាយការណ៍ចំណូល (UC8.6) ----------

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.isDeleted = false
              AND p.status = co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.VERIFIED
              AND p.type <> co.panha.hibernate.tourmanagement.features.payment.PaymentType.REFUND
              AND p.paidAt >= :from AND p.paidAt < :to
            """)
    BigDecimal revenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.isDeleted = false
              AND p.type = co.panha.hibernate.tourmanagement.features.payment.PaymentType.REFUND
              AND p.status IN (co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.PENDING,
                               co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.VERIFIED,
                               co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.REFUNDED)
              AND p.paidAt >= :from AND p.paidAt < :to
            """)
    BigDecimal refundedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(p) FROM Payment p
            WHERE p.isDeleted = false
              AND p.status = co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.VERIFIED
              AND p.type <> co.panha.hibernate.tourmanagement.features.payment.PaymentType.REFUND
              AND p.paidAt >= :from AND p.paidAt < :to
            """)
    long countBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** ចំណូលបែងចែកតាមវិធីទូទាត់ — ត្រឡប់ {@code [method, sum]} មួយជួរក្នុងមួយវិធី។ */
    @Query("""
            SELECT p.method, COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.isDeleted = false
              AND p.status = co.panha.hibernate.tourmanagement.features.payment.PaymentStatus.VERIFIED
              AND p.type <> co.panha.hibernate.tourmanagement.features.payment.PaymentType.REFUND
              AND p.paidAt >= :from AND p.paidAt < :to
            GROUP BY p.method
            """)
    List<Object[]> revenueByMethod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** ចំនួនការទូទាត់ក្នុងថ្ងៃមួយ — សម្រាប់បង្កើតលេខយោងតាមលំដាប់។ */
    @Query("""
            SELECT COUNT(p) FROM Payment p
            WHERE p.paidAt >= :dayStart AND p.paidAt < :nextDayStart
            """)
    long countPaymentsOn(@Param("dayStart") LocalDateTime dayStart,
                         @Param("nextDayStart") LocalDateTime nextDayStart);
}
