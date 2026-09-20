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

/**
 * កញ្ចប់ដំណើរកម្សាន្ត — ឈ្មោះ · តម្លៃ · រយៈពេល · ទីតាំង · រូបភាព។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tours")
public class Tour extends BaseEntity {

    /** លេខកូដសាធារណៈ — {@code TR-0001}។ */
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(unique = true, nullable = false, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** កម្មវិធីថ្ងៃទី ១, ថ្ងៃទី ២... */
    @Column(columnDefinition = "TEXT")
    private String itinerary;

    @Column(columnDefinition = "TEXT")
    private String included;

    @Column(columnDefinition = "TEXT")
    private String excluded;

    /** តម្លៃមូលដ្ឋាន — កាលវិភាគអាចជាន់លើដោយ {@code priceOverride} (F5)។ */
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

    /** គណនាទុកមុន — កែដោយ {@code recalculateRating} ក្រោយបង្កើត/កែ/លុប Review (F9)។ */
    private Double averageRating = 0.0;

    /** គណនាទុកមុន — ដូចគ្នានឹង {@code averageRating}។ */
    private Integer reviewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** ម្ចាស់ទំនាក់ទំនង M:N — ការកែពីខាង {@code Destination.tours} នឹងមិនរក្សាទុកទេ។ */
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

    // ចំណាំ៖ គ្មាន @OneToMany ទៅ TourSchedule ទេ — ការរាប់ និងការទាញធ្វើដោយ ScheduleRepository
    //   ព្រោះកាលវិភាគមួយ Tour អាចមានរាប់រយ ហើយភាគច្រើនជាអតីតកាលដែលមិនត្រូវការ។

    /**
     * ភ្ជាប់រូបភាពទាំងសងខាង។
     *
     * <p>ការ {@code images.add(image)} តែម្នាក់ឯងមិនគ្រប់គ្រាន់ទេ — {@code TourImage.tour}
     * ជាម្ចាស់ FK ដូច្នេះបើមិនកំណត់វា {@code tour_id} នឹងជា null ហើយការរក្សាទុកនឹងបរាជ័យ។
     */
    public void addImage(TourImage image) {
        image.setTour(this);
        images.add(image);
    }

    /** ដករូបភាពទាំងអស់ចេញ — {@code orphanRemoval} នឹងលុបជួរក្នុង database។ */
    public void clearImages() {
        images.forEach(image -> image.setTour(null));
        images.clear();
    }
}
