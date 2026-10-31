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

// Destination location; name is unique per province, so the same name can exist in different provinces.
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

    // NUMERIC(10,7): 3 digits before and 7 after the point, about 1 cm precision.
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String imageUrl;

    // No reverse @ManyToMany: counts come from TourRepository.countActiveByDestination().
}
