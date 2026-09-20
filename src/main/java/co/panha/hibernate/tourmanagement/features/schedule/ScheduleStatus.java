package co.panha.hibernate.tourmanagement.features.schedule;

/**
 * ស្ថានភាពកាលវិភាគចេញដំណើរ។
 *
 * <pre>
 * OPEN ──(កៅអីអស់)──> FULL ──(មានលុបចោល)──> OPEN
 *  │                    │
 *  │                    └──(ដល់ថ្ងៃចេញ)──> DEPARTED ──> COMPLETED
 *  ├──(admin បិទ)──> CLOSED
 *  └──(admin បោះបង់)──> CANCELLED   [ស្ថានភាពចុងក្រោយ]
 * </pre>
 *
 * <p>{@code FULL} កើតឡើងដោយស្វ័យប្រវត្តិពេលកៅអីអស់ — ត្រូវការ F7 Booking។
 * {@code DEPARTED} និង {@code COMPLETED} កំណត់ដោយ job ប្រចាំថ្ងៃ។
 */
public enum ScheduleStatus {
    OPEN,
    FULL,
    CLOSED,
    DEPARTED,
    COMPLETED,
    CANCELLED
}
