package co.panha.hibernate.tourmanagement.features.booking;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import co.panha.hibernate.tourmanagement.features.customer.Customer;
import co.panha.hibernate.tourmanagement.features.schedule.TourSchedule;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ការកក់ — F7។
 *
 * <p><b>Field ទឹកប្រាក់ជា snapshot</b>៖ {@code unitPrice} ចម្លងតម្លៃនៅ<b>ពេលកក់</b>
 * មិនមែនយោងទៅ {@code tour.price} ទេ។ បើយោង នោះការប្តូរតម្លៃ Tour ថ្ងៃស្អែកនឹងប្តូរ
 * ចំនួនប្រាក់នៃការកក់ចាស់ទាំងអស់ដោយស្ងាត់ — ជាកំហុសគណនេយ្យធ្ងន់ធ្ងរ។
 *
 * <p><b>{@code @Version} សំខាន់បំផុត</b>៖ ការកក់ត្រូវបានកែដោយច្រើនផ្លូវ (អតិថិជនកែអ្នកដំណើរ ·
 * admin បញ្ជាក់ · ការទូទាត់ចូល · job លុបចោលស្វ័យប្រវត្តិ)។ បើគ្មាន optimistic locking
 * ការសរសេរពីរព្រមគ្នានឹងធ្វើឲ្យ {@code paidAmount} ឬ {@code status} បាត់បង់ដោយស្ងាត់។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_booking_customer_status", columnList = "customer_id, status"),
                @Index(name = "idx_booking_schedule_status", columnList = "schedule_id, status"),
                @Index(name = "idx_booking_booked_at", columnList = "booked_at")
        }
)
public class Booking extends BaseEntity {

    /** លេខកូដសាធារណៈ — {@code BK-20260912-0001}។ ប្រើជាកូនសោលើ API ជំនួស {@code id}។ */
    @Column(unique = true, nullable = false, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule schedule;

    @Column(nullable = false)
    private Integer numberOfPeople;

    /** តម្លៃក្នុងមួយនាក់ — snapshot នៃ {@code schedule.effectivePrice()} នៅពេលកក់។ */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** {@code unitPrice × numberOfPeople} — មុនបញ្ចុះតម្លៃ។ */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subTotal;

    /** ការបញ្ចុះតម្លៃតាមទំហំក្រុម — BR10។ */
    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** {@code subTotal − discountAmount} — ចំនួនដែលត្រូវបង់ពិត។ */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    /** គណនាទុកមុន — បូកពី {@code payments} ដោយ F8។ */
    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingStatus status;

    @Column(length = 500)
    private String note;

    @Column(length = 500)
    private String cancelReason;

    @Column(nullable = false)
    private LocalDateTime bookedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Passenger> passengers = new ArrayList<>();

    /**
     * លេខកំណែសម្រាប់ optimistic locking។
     *
     * <p>Hibernate បង្កើន ១ រាល់ការ update ហើយបោះ {@code OptimisticLockingFailureException}
     * បើកំណែដែលអានមកមិនត្រូវនឹងកំណែក្នុង database — ដែល {@code GlobalAppException}
     * បម្លែងទៅ <b>409</b> រួចស្រេច។
     */
    @Version
    private Long version;

    // ចំណាំ៖ គ្មាន @OneToMany ទៅ Payment (F8) ឬ @OneToOne ទៅ Review (F9) ទេ
    //   ព្រោះ entity ទាំងនោះមិនទាន់មាន។ ត្រូវបន្ថែមពេលសរសេរ F8 និង F9។

    public void addPassenger(Passenger passenger) {
        passenger.setBooking(this);
        passengers.add(passenger);
    }

    /** លុបបញ្ជីអ្នកដំណើរទាំងស្រុង — {@code orphanRemoval} លុបជួរចាស់ចេញពី database។ */
    public void clearPassengers() {
        passengers.forEach(passenger -> passenger.setBooking(null));
        passengers.clear();
    }

    /** ចំនួនដែលនៅជំពាក់ — មិនរក្សាទុកក្នុង database ព្រោះទាញចេញបានពី ២ field។ */
    public BigDecimal remainingAmount() {
        return totalPrice.subtract(paidAmount == null ? BigDecimal.ZERO : paidAmount);
    }
}
