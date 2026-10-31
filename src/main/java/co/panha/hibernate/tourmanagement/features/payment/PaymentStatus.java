package co.panha.hibernate.tourmanagement.features.payment;

// Only VERIFIED counts toward booking.paidAmount; PENDING counts only in committed amount.
public enum PaymentStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    REFUNDED
}
