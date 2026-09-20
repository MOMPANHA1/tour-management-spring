package co.panha.hibernate.tourmanagement.features.destination;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * ទីតាំងគោលដៅ — សៀមរាប · កែប · មណ្ឌលគិរី · កោះរ៉ុង។
 *
 * <p>ឈ្មោះមិនស្ទួនក្នុងខេត្តតែមួយ ប៉ុន្តែ "ផ្សារចាស់" អាចមានទាំងភ្នំពេញ និងសៀមរាប។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "destinations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_destination_name_province",
                columnNames = {"name", "province"}
        )
)
public class Destination extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String province;

    @Column(nullable = false, length = 80)
    private String country;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** {@code NUMERIC(10,7)} — ៣ ខ្ទង់មុនចុច + ៧ ខ្ទង់ក្រោយ ≈ ភាពជាក់លាក់ ១ សង់ទីម៉ែត្រ។ */
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String imageUrl;

    // ចំណាំ៖ គ្មាន @ManyToMany បញ្ច្រាសទេ — ការរាប់ធ្វើដោយ TourRepository.countActiveByDestination()។
}
