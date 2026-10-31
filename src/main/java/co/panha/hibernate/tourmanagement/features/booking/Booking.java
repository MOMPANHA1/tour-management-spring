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

// F7 booking; amounts are snapshots taken at booking time, @Version guards concurrent updates.
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

    // Public code (BK-20260912-0001), used as the API key instead of id.
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

    // Per-person price, snapshot of schedule.effectivePrice() at booking time.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // unitPrice x numberOfPeople, before discount.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subTotal;

    // Group-size discount (BR10).
    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // subTotal - discountAmount: the amount actually payable.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    // Precomputed sum of payments, maintained by F8.
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

    // Optimistic locking version; a stale write fails and is mapped to HTTP 409.
    @Version
    private Long version;

    // TODO: add @OneToMany to Payment (F8) and @OneToOne to Review (F9) once those entities exist.

    public void addPassenger(Passenger passenger) {
        passenger.setBooking(this);
        passengers.add(passenger);
    }

    // orphanRemoval deletes the old passenger rows from the database.
    public void clearPassengers() {
        passengers.forEach(passenger -> passenger.setBooking(null));
        passengers.clear();
    }

    // Outstanding amount; derived from two fields, so not persisted.
    public BigDecimal remainingAmount() {
        return totalPrice.subtract(paidAmount == null ? BigDecimal.ZERO : paidAmount);
    }
}
