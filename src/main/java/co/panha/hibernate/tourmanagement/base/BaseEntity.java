package co.panha.hibernate.tourmanagement.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Field រួមគ្រប់ Entity — {@code id}, {@code uuid}, {@code isDeleted}, {@code createdAt}, {@code updatedAt}។
 *
 * <p>{@code @MappedSuperclass} មិនបង្កើតតារាងទេ — column ទាំង ៥ ចម្លងចូលតារាងកូនម្នាក់ៗ។
 *
 * <p>{@code createdAt} និង {@code updatedAt} បំពេញដោយ Spring Data Auditing ដែលបើកនៅ
 * {@code config/JpaAuditingConfig}។ បើ config នោះបាត់ តម្លៃទាំងពីរនឹង null ជានិច្ចដោយគ្មានកំហុសបង្ហាញ។
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /** សោខាងក្នុង — មិនបង្ហាញលើ API។ */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** សោសាធារណៈ — ប្រើលើ API ជំនួស {@code id} ដើម្បីកុំឲ្យទាយលេខ resource អ្នកដទៃបាន។ */
    @Column(unique = true, nullable = false, length = 36)
    private String uuid;

    /** Soft delete — គ្រប់ query ត្រូវច្រោះ {@code isDeleted = false}។ */
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * សុវត្ថិភាពបន្ថែម៖ បំពេញ {@code uuid} និង {@code isDeleted} បើ Service ភ្លេចកំណត់។
     * Service នៅតែអាចកំណត់ {@code uuid} ដោយខ្លួនឯង — តម្លៃដែលមានស្រាប់មិនត្រូវជាន់ទេ។
     */
    @PrePersist
    protected void onPrePersist() {
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
}
