package co.panha.hibernate.tourmanagement.features.payment;

// Online methods are verified immediately; CASH and BANK_TRANSFER wait for ADMIN review.
public enum PaymentMethod {
    CASH,
    BANK_TRANSFER,
    ABA_PAY,
    WING,
    CREDIT_CARD,
    KHQR
}
