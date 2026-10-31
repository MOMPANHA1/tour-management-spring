package co.panha.hibernate.tourmanagement.features.category;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Tour category such as nature, culture, sea or adventure.
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "categories")
public class Category extends BaseEntity {

    // No @Column(unique=true): uniqueness is a partial index (ux_categories_name_active) on non-deleted rows only.
    @Column(nullable = false, length = 80)
    private String name;

    // Generated from name; unique via partial index ux_categories_slug_active so soft-deleted rows free their slug.
    @Column(nullable = false, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String iconUrl;

    // No @OneToMany to Tour: counts come from TourRepository.countActiveByCategory() to avoid loading all tours.
}
