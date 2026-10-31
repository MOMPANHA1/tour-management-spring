package co.panha.hibernate.tourmanagement.features.tour;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import co.panha.hibernate.tourmanagement.features.category.Category;
import co.panha.hibernate.tourmanagement.features.destination.Destination;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Tour package: name, price, duration, destinations, images.
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tours")
public class Tour extends BaseEntity {

    // Public code, e.g. TR-0001.
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(unique = true, nullable = false, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Day-by-day program (day 1, day 2, ...).
    @Column(columnDefinition = "TEXT")
    private String itinerary;

    @Column(columnDefinition = "TEXT")
    private String included;

    @Column(columnDefinition = "TEXT")
    private String excluded;

    // Base price; a schedule can override it with priceOverride (F5).
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer durationDays;

    private Integer durationNights;

    private Integer minGroupSize = 1;

    @Column(nullable = false)
    private Integer maxGroupSize;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty;

    @Column(length = 255)
    private String thumbnailUrl;

    @Column(nullable = false)
    private Boolean isPublished = false;

    // Precomputed; updated by recalculateRating after review create/update/delete (F9).
    private Double averageRating = 0.0;

    // Precomputed, same as averageRating.
    private Integer reviewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Owning side of the M:N relation; edits from Destination.tours are not persisted.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tour_destinations",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "destination_id")
    )
    private Set<Destination> destinations = new LinkedHashSet<>();

    @OrderBy("sortOrder ASC")
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TourImage> images = new ArrayList<>();

    // Note: no @OneToMany to TourSchedule; ScheduleRepository handles it since a tour can have hundreds, mostly past.

    // Sets both sides: TourImage.tour owns the FK, so it must be set or tour_id is null on save.
    public void addImage(TourImage image) {
        image.setTour(this);
        images.add(image);
    }

    // Remove all images; orphanRemoval deletes the rows.
    public void clearImages() {
        images.forEach(image -> image.setTour(null));
        images.clear();
    }
}
