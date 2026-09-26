package co.panha.hibernate.tourmanagement.features.customer;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import co.panha.hibernate.tourmanagement.base.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * អតិថិជន — F6។
 *
 * <p><b>គ្មាន column {@code password}</b> — ការផ្ទៀងផ្ទាត់អត្តសញ្ញាណធ្វើដោយ Keycloak។
 * តារាងនេះកាន់តែប្រវត្តិរូប (profile) ប៉ុណ្ណោះ។
 *
 * <p><b>ស្ពានភ្ជាប់គឺ {@link #keycloakId}</b> មិនមែន {@link #username} ទេ — ព្រោះ username
 * អាចប្តូរបានក្នុង Keycloak តែ {@code sub} (UUID) មិនប្តូរជារៀងរហូត។ បើប្រើ username ជាស្ពាន
 * នោះអតិថិជនប្តូរឈ្មោះម្តង គឺបាត់ទំនាក់ទំនងនឹងការកក់ទាំងអស់។
 *
 * <p>រក្សា {@code Long id} ពី {@code BaseEntity} ដដែល ដូច្នេះ FK {@code BIGINT} ក្នុង
 * {@code bookings} និង {@code reviews} មិនចាំបាច់ប្តូរទេ។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "customers")
public class Customer extends BaseEntity {

    /** Keycloak User ID ({@code sub} claim) — UUID ប្រវែង ៣៦ តួ។ */
    @Column(unique = true, nullable = false, length = 36)
    private String keycloakId;

    @Column(unique = true, nullable = false, length = 60)
    private String username;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(unique = true, nullable = false, length = 120)
    private String email;

    @Column(unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    private LocalDate dateOfBirth;

    @Column(length = 80)
    private String nationality;

    /** លេខលិខិតឆ្លងដែន — ទិន្នន័យរសើប មិនត្រឡប់ក្នុង {@code CustomerResponse} ទេ។ */
    @Column(length = 40)
    private String passportNo;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CustomerStatus status;

    // ចំណាំ៖ គ្មាន @OneToMany ទៅ Booking ឬ Review ទេ — ការរាប់ធ្វើដោយ repository
    //   ដូចលំនាំ Guide ដែរ (មើល CustomerServiceImpl.toResponseWithStats)។
}
