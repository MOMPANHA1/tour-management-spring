package co.panha.hibernate.tourmanagement.features.payment;

/**
 * ស្ថានភាពការទូទាត់។
 *
 * <p>មានតែ {@code VERIFIED} ប៉ុណ្ណោះដែលរាប់ចូល {@code booking.paidAmount}។
 * {@code PENDING} រាប់ចូលតែក្នុងការគណនា «ចំនួនដែលបានប្តេជ្ញា» ដើម្បីការពារការបង់លើស។
 */
public enum PaymentStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    REFUNDED
}
