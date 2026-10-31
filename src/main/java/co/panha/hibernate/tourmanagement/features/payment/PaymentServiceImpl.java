package co.panha.hibernate.tourmanagement.features.payment;

import co.panha.hibernate.tourmanagement.features.booking.Booking;
import co.panha.hibernate.tourmanagement.features.booking.BookingRepository;
import co.panha.hibernate.tourmanagement.features.booking.BookingStatus;
import co.panha.hibernate.tourmanagement.features.payment.dto.CreatePaymentRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.CreateRefundRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.PaymentResponse;
import co.panha.hibernate.tourmanagement.features.payment.dto.PaymentSummaryResponse;
import co.panha.hibernate.tourmanagement.features.payment.dto.RejectPaymentRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.RevenueReportResponse;
import co.panha.hibernate.tourmanagement.security.AuthUtils;
import co.panha.hibernate.tourmanagement.utils.GenerateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Uses BookingRepository, not BookingService, to avoid a circular dependency (F7 calls refunds on cancel).
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final String CODE_PREFIX = "PM";
    private static final String SYSTEM_VERIFIER = "SYSTEM";

    // BR4 - deposit must be at least 30% of total price.
    private static final BigDecimal MIN_DEPOSIT_RATE = new BigDecimal("0.30");

    // Methods verified immediately; WARNING: no real gateway check, switch to webhook-driven VERIFIED later.
    private static final Set<PaymentMethod> AUTO_VERIFIED_METHODS =
            Set.of(PaymentMethod.ABA_PAY, PaymentMethod.KHQR,
                    PaymentMethod.CREDIT_CARD, PaymentMethod.WING);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;

    // UC8.1 - remaining amount counts PENDING too, so double submissions cannot overpay.
    @Override
    @Transactional
    public PaymentResponse pay(String bookingCode, CreatePaymentRequest request) {

        // 2. Load
        Booking booking = loadBookingForCurrentUser(bookingCode, false);

        // 3. Check rules
        requirePayableBooking(booking);

        if (request.type() == PaymentType.REFUND) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use the refund endpoint instead");
        }

        if (request.method() == PaymentMethod.BANK_TRANSFER
                && (request.receiptUrl() == null || request.receiptUrl().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A receipt URL is required for bank transfers");
        }

        if (request.transactionId() != null && !request.transactionId().isBlank()
                && paymentRepository.existsByTransactionId(request.transactionId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This transaction id has already been used");
        }

        // 4. Calculate
        BigDecimal remaining = remainingToCommit(booking);

        if (remaining.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This booking is already fully paid");
        }

        if (request.amount().compareTo(remaining) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Amount exceeds the outstanding balance — " + remaining + " remaining");
        }

        requireAmountMatchesType(request, booking, remaining);

        // 5. Build
        Payment payment = new Payment();
        payment.setReferenceNo(nextPaymentReference());
        payment.setBooking(booking);
        payment.setType(request.type());
        payment.setMethod(request.method());
        payment.setAmount(request.amount());
        payment.setCurrency("USD");
        payment.setTransactionId(emptyToNull(request.transactionId()));
        payment.setReceiptUrl(emptyToNull(request.receiptUrl()));
        payment.setNote(request.note());
        payment.setPaidAt(LocalDateTime.now());
        payment.setIsDeleted(false);

        if (AUTO_VERIFIED_METHODS.contains(request.method())) {
            payment.setStatus(PaymentStatus.VERIFIED);
            payment.setVerifiedAt(LocalDateTime.now());
            payment.setVerifiedBy(SYSTEM_VERIFIER);
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }

        // 6. Save
        Payment saved = paymentRepository.saveAndFlush(payment);

        // 7. Side effects
        if (saved.getStatus() == PaymentStatus.VERIFIED) {
            syncBookingPaidAmount(booking);
        }

        // TODO: stage 5 - notificationService.sendPaymentReceived(saved)
        log.info("Payment {} recorded for booking {} — {} {} ({})",
                saved.getReferenceNo(), booking.getCode(), saved.getAmount(),
                saved.getMethod(), saved.getStatus());

        return paymentMapper.toResponse(saved);
    }

    @Override
    public PaymentSummaryResponse getSummary(String bookingCode) {

        Booking booking = loadBookingForCurrentUser(bookingCode, true);

        BigDecimal verified = paymentRepository.sumVerifiedAmount(booking.getId());
        BigDecimal pending = paymentRepository.sumPendingAmount(booking.getId());
        BigDecimal refunded = paymentRepository.sumRefundedAmount(booking.getId());
        BigDecimal remaining = booking.getTotalPrice().subtract(verified.subtract(refunded));

        List<PaymentResponse> payments = paymentMapper.toResponses(
                paymentRepository.findAllByBookingIdAndIsDeletedFalseOrderByCreatedAtDesc(booking.getId()));

        return new PaymentSummaryResponse(
                booking.getCode(),
                booking.getTotalPrice(),
                verified,
                pending,
                refunded,
                remaining,
                remaining.signum() <= 0,
                payments
        );
    }

    @Override
    @Transactional
    public PaymentResponse verify(String referenceNo) {

        Payment payment = loadByReference(referenceNo);
        requirePendingPayment(payment, "verified");

        payment.setStatus(PaymentStatus.VERIFIED);
        payment.setVerifiedAt(LocalDateTime.now());
        payment.setVerifiedBy(AuthUtils.currentUsername());
        paymentRepository.saveAndFlush(payment);

        syncBookingPaidAmount(payment.getBooking());

        // TODO: stage 5 - notificationService.sendPaymentVerified(payment)

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse reject(String referenceNo, RejectPaymentRequest request) {

        Payment payment = loadByReference(referenceNo);
        requirePendingPayment(payment, "rejected");

        payment.setStatus(PaymentStatus.REJECTED);
        payment.setRejectReason(request.reason());
        payment.setVerifiedAt(LocalDateTime.now());
        payment.setVerifiedBy(AuthUtils.currentUsername());
        paymentRepository.saveAndFlush(payment);

        // Rejecting a refund releases the obligation, so recompute paidAmount
        if (payment.getType() == PaymentType.REFUND) {
            syncBookingPaidAmount(payment.getBooking());
        }

        // TODO: stage 5 - notificationService.sendPaymentRejected(payment)

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse createRefund(String bookingCode, CreateRefundRequest request) {

        Booking booking = bookingRepository.findByCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found with code = " + bookingCode));

        Payment refund = buildRefund(booking, request.amount(), request.reason(), request.method());

        return paymentMapper.toResponse(refund);
    }

    // Refund for other features; does not throw when there is nothing to refund (unlike createRefund).
    @Override
    @Transactional
    public BigDecimal refundForBooking(Booking booking, BigDecimal amount, String reason) {

        BigDecimal maxRefundable = maxRefundable(booking);

        if (maxRefundable.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal target = (amount == null) ? maxRefundable : amount.min(maxRefundable);

        if (target.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        buildRefund(booking, target, reason, PaymentMethod.BANK_TRANSFER);

        return target;
    }

    @Override
    public RevenueReportResponse getRevenueReport(LocalDate fromDate, LocalDate toDate) {

        LocalDate from = (fromDate != null) ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to = (toDate != null) ? toDate : LocalDate.now();

        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "toDate must be on or after fromDate");
        }

        // Range query instead of DATE(paidAt): not standard JPQL and breaks the paid_at index.
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        BigDecimal totalRevenue = paymentRepository.revenueBetween(start, end);
        BigDecimal totalRefunded = paymentRepository.refundedBetween(start, end);

        Map<PaymentMethod, BigDecimal> byMethod = new EnumMap<>(PaymentMethod.class);
        for (Object[] row : paymentRepository.revenueByMethod(start, end)) {
            byMethod.put((PaymentMethod) row[0], (BigDecimal) row[1]);
        }

        return new RevenueReportResponse(
                from,
                to,
                totalRevenue,
                totalRefunded,
                totalRevenue.subtract(totalRefunded),
                paymentRepository.countBetween(start, end),
                byMethod
        );
    }

    // ---------- Business rules ----------

    // BR2 - cannot pay a cancelled or completed booking.
    private void requirePayableBooking(Booking booking) {

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking has been cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This tour has already been completed");
        }
    }

    // BR4 and FULL_PAYMENT rule.
    private void requireAmountMatchesType(CreatePaymentRequest request, Booking booking, BigDecimal remaining) {

        if (request.type() == PaymentType.DEPOSIT) {
            BigDecimal minDeposit = booking.getTotalPrice()
                    .multiply(MIN_DEPOSIT_RATE)
                    .setScale(2, RoundingMode.HALF_UP);

            if (request.amount().compareTo(minDeposit) < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                        "A deposit must be at least 30% (" + minDeposit + ")");
            }
        }

        if (request.type() == PaymentType.FULL_PAYMENT
                && request.amount().compareTo(remaining) != 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "FULL_PAYMENT must equal the outstanding balance (" + remaining + ")");
        }
    }

    // BR6 - only PENDING payments can be verified or rejected.
    private void requirePendingPayment(Payment payment, String action) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only PENDING payments can be " + action + " (current: " + payment.getStatus() + ")");
        }
    }

    // ---------- Internal helpers ----------

    // BR7 - refund cannot exceed the amount actually paid.
    private BigDecimal maxRefundable(Booking booking) {
        return paymentRepository.sumVerifiedAmount(booking.getId())
                .subtract(paymentRepository.sumRefundedAmount(booking.getId()));
    }

    private Payment buildRefund(Booking booking, BigDecimal amount, String reason, PaymentMethod method) {

        BigDecimal maxRefundable = maxRefundable(booking);

        if (amount.compareTo(maxRefundable) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "At most " + maxRefundable + " can be refunded");
        }

        Payment refund = new Payment();
        refund.setReferenceNo(nextPaymentReference());
        refund.setBooking(booking);
        refund.setType(PaymentType.REFUND);
        refund.setMethod(method);
        refund.setAmount(amount);
        refund.setCurrency("USD");
        // PENDING until finance actually transfers the money out
        refund.setStatus(PaymentStatus.PENDING);
        refund.setNote(reason);
        refund.setPaidAt(LocalDateTime.now());
        refund.setIsDeleted(false);

        Payment saved = paymentRepository.saveAndFlush(refund);

        syncBookingPaidAmount(booking);

        // TODO: stage 5 - notificationService.sendRefundInitiated(saved)
        log.info("Refund {} created for booking {} — {} ({})",
                saved.getReferenceNo(), booking.getCode(), saved.getAmount(), reason);

        return saved;
    }

    // Recomputes booking.paidAmount from payments; fully paid + PENDING booking auto-becomes CONFIRMED.
    private void syncBookingPaidAmount(Booking booking) {

        BigDecimal verified = paymentRepository.sumVerifiedAmount(booking.getId());
        BigDecimal refunded = paymentRepository.sumRefundedAmount(booking.getId());

        booking.setPaidAmount(verified.subtract(refunded));

        if (booking.getPaidAmount().compareTo(booking.getTotalPrice()) >= 0
                && booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setConfirmedAt(LocalDateTime.now());

            log.info("Booking {} auto-confirmed — fully paid ({})",
                    booking.getCode(), booking.getPaidAmount());

            // TODO: stage 5 - notificationService.sendBookingConfirmed(booking)
        }

        bookingRepository.save(booking);
    }

    // committed = verified + pending - refunded; remaining = totalPrice - committed
    private BigDecimal remainingToCommit(Booking booking) {

        BigDecimal committed = paymentRepository.sumVerifiedAmount(booking.getId())
                .add(paymentRepository.sumPendingAmount(booking.getId()))
                .subtract(paymentRepository.sumRefundedAmount(booking.getId()));

        return booking.getTotalPrice().subtract(committed);
    }

    private Payment loadByReference(String referenceNo) {
        return paymentRepository.findByReferenceNoAndIsDeletedFalse(referenceNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment not found with reference = " + referenceNo));
    }

    // Loads booking and checks ownership; throws 404 rather than 403 to match BookingServiceImpl.
    private Booking loadBookingForCurrentUser(String bookingCode, boolean allowAdmin) {

        Booking booking = bookingRepository.findByCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found with code = " + bookingCode));

        if (allowAdmin && AuthUtils.isAdmin()) {
            return booking;
        }

        if (!booking.getCustomer().getKeycloakId().equals(AuthUtils.currentKeycloakId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Booking not found with code = " + bookingCode);
        }

        return booking;
    }

    private String nextPaymentReference() {

        LocalDate today = LocalDate.now();
        long count = paymentRepository.countPaymentsOn(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        String reference = GenerateUtils.generateDateSequentialCode(CODE_PREFIX, today, count);
        while (paymentRepository.existsByReferenceNo(reference)) {
            count++;
            reference = GenerateUtils.generateDateSequentialCode(CODE_PREFIX, today, count);
        }
        return reference;
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
