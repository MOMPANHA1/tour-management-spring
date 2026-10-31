package co.panha.hibernate.tourmanagement.features.schedule;

// Lifecycle: OPEN <-> FULL, then DEPARTED -> COMPLETED (daily job); admin sets CLOSED/CANCELLED (final).
public enum ScheduleStatus {
    OPEN,
    FULL,
    CLOSED,
    DEPARTED,
    COMPLETED,
    CANCELLED
}
