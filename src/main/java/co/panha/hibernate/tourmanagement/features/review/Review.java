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

// Review (F9); tour/customer are denormalized from booking; booking is unique to enforce BR3 against races.
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

    // Overall score 1-5; the only score feeding tour.averageRating.
    @Column(nullable = false)
    private Integer rating;

    // Guide score 1-5, optional.
    private Integer guideRating;

    // Value-for-money score 1-5, optional.
    private Integer valueRating;

    @Column(length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Image URLs; explicit table name keeps it stable across version changes.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "review_image_urls",
            joinColumns = @JoinColumn(name = "review_id")
    )
    @Column(name = "image_url", length = 255)
    private List<String> imageUrls = new ArrayList<>();

    // Hidden by ADMIN (distinct from isDeleted); kept for audit but excluded from average and public lists.
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
