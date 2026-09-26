package co.panha.hibernate.tourmanagement.features.review;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import co.panha.hibernate.tourmanagement.features.booking.Booking;
import co.panha.hibernate.tourmanagement.features.customer.Customer;
import co.panha.hibernate.tourmanagement.features.tour.Tour;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ការវាយតម្លៃ — F9។
 *
 * <p><b>{@code tour} និង {@code customer} ទាញចេញពី {@code booking}</b> មិនមែនទទួលពី client ទេ។
 * ពួកវាធ្វើឲ្យទិន្នន័យស្ទួនដោយចេតនា (denormalized) ដើម្បីឲ្យការសួរតាម Tour លឿន
 * ដោយមិនចាំបាច់ join ឆ្លងកាត់ {@code bookings → tour_schedules → tours}។
 *
 * <p><b>{@code booking} ជា unique</b> — ការកក់មួយវាយតម្លៃបានតែម្តង (BR3)។ ការដាក់
 * unique constraint នៅកម្រិត database ការពារ race condition ដែល {@code existsByBookingId}
 * តែម្នាក់ឯងមិនអាចការពារបាន។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_review_tour_visible", columnList = "tour_id, is_visible"),
                @Index(name = "idx_review_customer", columnList = "customer_id")
        }
)
public class Review extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** ពិន្ទុរួម ១–៥ — ជាពិន្ទុតែមួយគត់ដែលចូលក្នុង {@code tour.averageRating}។ */
    @Column(nullable = false)
    private Integer rating;

    /** ពិន្ទុមគ្គុទ្ទេសក៍ ១–៥ — ជាជម្រើស។ */
    private Integer guideRating;

    /** ពិន្ទុតម្លៃសមរម្យ ១–៥ — ជាជម្រើស។ */
    private Integer valueRating;

    @Column(length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * URL រូបភាព — ដាក់ឈ្មោះតារាងច្បាស់លាស់ដូចលំនាំ {@code guide_languages} ដែរ
     * ដើម្បីកុំឲ្យឈ្មោះប្រែប្រួលពេលប្តូរ version។
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "review_image_urls",
            joinColumns = @JoinColumn(name = "review_id")
    )
    @Column(name = "image_url", length = 255)
    private List<String> imageUrls = new ArrayList<>();

    /**
     * ការលាក់ដោយ ADMIN — ខុសពី {@code isDeleted} ដែលជាការលុបដោយអតិថិជន។
     *
     * <p>ការវាយតម្លៃដែលលាក់នៅតែមានក្នុង database (សម្រាប់ការត្រួតពិនិត្យ) តែមិនរាប់
     * ចូលពិន្ទុមធ្យម ហើយមិនបង្ហាញជាសាធារណៈទេ។
     */
    @Column(nullable = false)
    private Boolean isVisible = true;

    @Column(length = 255)
    private String hiddenReason;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    private LocalDateTime repliedAt;

    @Column(length = 60)
    private String repliedBy;
}
