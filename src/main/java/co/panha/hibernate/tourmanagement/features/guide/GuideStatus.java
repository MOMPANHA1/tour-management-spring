package co.panha.hibernate.tourmanagement.features.guide;

// Guide status: ACTIVE can be assigned, ON_LEAVE is temporary, INACTIVE has left (blocked if future schedules).
public enum GuideStatus {
    ACTIVE,
    ON_LEAVE,
    INACTIVE
}
