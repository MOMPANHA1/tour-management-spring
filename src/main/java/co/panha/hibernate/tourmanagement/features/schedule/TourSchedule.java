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

/**
 * កាលវិភាគចេញដំណើរជាក់លាក់របស់ Tour មួយ។
 *
 * <p>នេះជា entity ដែល <b>ការកក់ភ្ជាប់ជាមួយ</b> — មិនមែន Tour ទេ។ Tour ជាកញ្ចប់លក់
 * ចំណែក TourSchedule ជាថ្ងៃចេញដំណើរពិតដែលមានចំនួនកៅអីកំណត់។
 */
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

    /** លេខកូដសាធារណៈ — {@code SC-20260912-07}។ */
    @Column(unique = true, nullable = false, length = 25)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    /** អាច null ដំបូង — Admin ចាត់តាំងមគ្គុទ្ទេសក៍ក្រោយបាន។ */
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

    /**
     * ចំនួនកៅអីសម្រាប់ថ្ងៃចេញនេះ។
     *
     * <p>អាចតិចជាង {@code tour.maxGroupSize} — ឧ. Tour ទទួលបាន ២០ នាក់ តែរថយន្តថ្ងៃនោះមាន ១៥ កៅអី។
     */
    @Column(nullable = false)
    private Integer capacity;

    /** {@code null} = ប្រើ {@code tour.price}។ */
    @Column(precision = 12, scale = 2)
    private BigDecimal priceOverride;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ScheduleStatus status;

    @Column(length = 500)
    private String cancelReason;

    // ចំណាំ៖ គ្មាន @OneToMany ទៅ Booking ទេ — ការរាប់កៅអីធ្វើដោយ
    //   BookingRepository.countOccupiedSeats() (F7) ព្រោះវាត្រូវច្រោះតាមស្ថានភាពការកក់។

    /** តម្លៃពិតដែលអតិថិជនបង់សម្រាប់ថ្ងៃចេញនេះ។ */
    public BigDecimal effectivePrice() {
        return (priceOverride != null) ? priceOverride : tour.getPrice();
    }
}
