package co.panha.hibernate.tourmanagement.features.booking;

import co.panha.hibernate.tourmanagement.base.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * អ្នកដំណើរម្នាក់ក្នុងការកក់មួយ។
 *
 * <p><b>មិនពង្រីក {@code BaseEntity}</b> ដោយចេតនា — វាជា entity កូនដែលគ្មានអត្ថិភាព
 * ដោយឡែកពី {@code Booking} ទេ។ គ្មាន soft delete ព្រោះការលុបធ្វើតាម {@code orphanRemoval}
 * ពេលអតិថិជនកែបញ្ជីអ្នកដំណើរ។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "passengers")
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    private LocalDate dateOfBirth;

    @Column(length = 40)
    private String passportNo;

    /** អ្នកដំណើរមេ — ជាអ្នកទទួលការទាក់ទង។ ត្រូវមានតែម្នាក់ក្នុងការកក់មួយ។ */
    @Column(nullable = false)
    private Boolean isLead = false;
}
