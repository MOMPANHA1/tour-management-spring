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

// F6 customer profile; no password column (Keycloak authenticates) and keycloakId (the sub UUID) links to the user.
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "customers")
public class Customer extends BaseEntity {

    // Keycloak user ID (sub claim), a 36-char UUID; stable unlike username.
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

    // Sensitive; never returned in CustomerResponse.
    @Column(length = 40)
    private String passportNo;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CustomerStatus status;

    // No @OneToMany to Booking or Review; counts are done in the repository (see CustomerServiceImpl.toResponseWithStats).
}
