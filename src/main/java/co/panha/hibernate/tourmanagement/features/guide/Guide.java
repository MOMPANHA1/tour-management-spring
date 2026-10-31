package co.panha.hibernate.tourmanagement.features.guide;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import co.panha.hibernate.tourmanagement.base.Gender;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

// Tour guide.
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "guides")
public class Guide extends BaseEntity {

    // Public code such as GD-0001.
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Column(unique = true, length = 120)
    private String email;

    // Spoken languages; explicit table name keeps it stable across versions; EAGER as the list is small and always returned.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "guide_languages",
            joinColumns = @JoinColumn(name = "guide_id")
    )
    @Column(name = "language", length = 10)
    private Set<String> languages = new LinkedHashSet<>();

    private Integer yearsExperience;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 255)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GuideStatus status;

    // No @OneToMany to TourSchedule: counts come from ScheduleRepository.countByGuideIdAndIsDeletedFalse().
}
