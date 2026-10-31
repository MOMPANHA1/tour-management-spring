package co.panha.hibernate.tourmanagement.features.schedule;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import co.panha.hibernate.tourmanagement.features.guide.Guide;
import co.panha.hibernate.tourmanagement.features.tour.Tour;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// A concrete departure of a tour; bookings attach to this, not to the Tour package.
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "tour_schedules",
        indexes = {
                @Index(name = "idx_schedule_tour_date", columnList = "tour_id, departure_date"),
                @Index(name = "idx_schedule_status_date", columnList = "status, departure_date"),
                @Index(name = "idx_schedule_guide_dates", columnList = "guide_id, departure_date, return_date")
        }
)
public class TourSchedule extends BaseEntity {

    // Public code, e.g. SC-20260912-07.
    @Column(unique = true, nullable = false, length = 25)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    // Nullable: admin can assign a guide later.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    private Guide guide;

    @Column(nullable = false)
    private LocalDate departureDate;

    @Column(nullable = false)
    private LocalDate returnDate;

    private LocalTime departureTime;

    @Column(length = 255)
    private String meetingPoint;

    // Seats for this departure; may be below tour.maxGroupSize (e.g. smaller vehicle).
    @Column(nullable = false)
    private Integer capacity;

    // null means use tour.price.
    @Column(precision = 12, scale = 2)
    private BigDecimal priceOverride;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ScheduleStatus status;

    @Column(length = 500)
    private String cancelReason;

    // Note: no @OneToMany to Booking; seats are counted via BookingRepository.countOccupiedSeats() (F7).

    // Actual price the customer pays for this departure.
    public BigDecimal effectivePrice() {
        return (priceOverride != null) ? priceOverride : tour.getPrice();
    }
}
