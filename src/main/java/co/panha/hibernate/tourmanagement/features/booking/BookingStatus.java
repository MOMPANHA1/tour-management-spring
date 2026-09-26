package co.panha.hibernate.tourmanagement.features.booking;

/**
 * វដ្តជីវិតនៃការកក់។
 *
 * <pre>
 *   [កក់] ──> PENDING ──(admin confirm)──> CONFIRMED ──(ដំណើរបញ្ចប់)──> COMPLETED
 *                │                            │
 *                └──(លុបចោល)──> CANCELLED <───┘
 * </pre>
 *
 * <p>{@code PENDING} ត្រូវបានលុបចោលដោយស្វ័យប្រវត្តិបើមិនបង់ប្រាក់ក្នុង ២៤ ម៉ោង
 * (មើល {@code BookingServiceImpl.autoCancelExpiredPending})។
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
