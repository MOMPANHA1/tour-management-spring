package co.panha.hibernate.tourmanagement.features.customer;

// SUSPENDED is temporary, BLOCKED is long-term; both disable the Keycloak user and differ only for reporting.
public enum CustomerStatus {
    ACTIVE,
    SUSPENDED,
    BLOCKED
}
