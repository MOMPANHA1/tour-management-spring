package co.panha.hibernate.tourmanagement.features.customer;

/**
 * ស្ថានភាពគណនីអតិថិជន។
 *
 * <p>{@code SUSPENDED} ជាការផ្អាកបណ្តោះអាសន្ន (អាចដោះវិញ) ចំណែក {@code BLOCKED} ជាការហាមឃាត់
 * រយៈពេលវែង។ ទាំងពីរបិទ user ក្នុង Keycloak ដូចគ្នា — ភាពខុសគ្នាគឺក្នុងការរាយការណ៍ប៉ុណ្ណោះ។
 */
public enum CustomerStatus {
    ACTIVE,
    SUSPENDED,
    BLOCKED
}
