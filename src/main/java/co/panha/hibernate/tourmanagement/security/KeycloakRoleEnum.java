package co.panha.hibernate.tourmanagement.security;

// Realm roles created manually in Keycloak; names must match exactly since toString() is used for lookup.
public enum KeycloakRoleEnum {
    USER,
    CUSTOMER,
    ADMIN
}
