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

/**
 * មគ្គុទ្ទេសក៍ទេសចរណ៍។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "guides")
public class Guide extends BaseEntity {

    /** លេខកូដសាធារណៈ — {@code GD-0001}។ */
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

    /**
     * ភាសាដែលនិយាយបាន — {@code ["KH", "EN", "ZH"]}។
     *
     * <p>ដាក់ឈ្មោះតារាងច្បាស់លាស់ជំនួសឲ្យទុកឲ្យ Hibernate ដាក់លំនាំដើម ដើម្បីកុំឲ្យឈ្មោះប្រែប្រួល
     * ពេលប្តូរ version។ {@code EAGER} ព្រោះបញ្ជីភាសាតូច ហើយត្រូវបង្ហាញជានិច្ចក្នុង response។
     */
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

    // ចំណាំ៖ គ្មាន @OneToMany ទៅ TourSchedule ទេ — ការរាប់ធ្វើដោយ
    //   ScheduleRepository.countByGuideIdAndIsDeletedFalse()។
}
