package co.panha.hibernate.tourmanagement.features.payment;

// REFUND subtracts from booking.paidAmount; all totals must exclude REFUND separately.
public enum PaymentType {
    DEPOSIT,
    FULL_PAYMENT,
    BALANCE,
    REFUND
}
