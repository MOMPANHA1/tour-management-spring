package co.panha.hibernate.tourmanagement.features.booking;

// Lifecycle: PENDING -> CONFIRMED -> COMPLETED; PENDING/CONFIRMED -> CANCELLED (PENDING auto-cancels after 24h).
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
