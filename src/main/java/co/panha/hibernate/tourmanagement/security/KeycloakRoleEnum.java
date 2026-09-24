package co.panha.hibernate.tourmanagement.security;

/**
 * Realm role ដែលត្រូវបង្កើតដោយដៃក្នុង Keycloak Admin Console។
 *
 * <p>ឈ្មោះ constant ត្រូវ<b>ដូចបេះបិទ</b>នឹងឈ្មោះ role ក្នុង realm ព្រោះ
 * {@code AuthServiceImpl} ប្រើ {@code toString()} ទៅទាញ role តាមឈ្មោះ។
 *
 * <p>{@code USER} ជា role មូលដ្ឋានផ្តល់ឲ្យគ្រប់គ្នា · {@code CUSTOMER} សម្រាប់អ្នកកក់ដំណើរ ·
 * {@code ADMIN} សម្រាប់អ្នកគ្រប់គ្រងទិន្នន័យមេ (Category · Destination · Guide · Tour · Schedule)។
 */
public enum KeycloakRoleEnum {
    USER,
    CUSTOMER,
    ADMIN
}
