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

/**
 * Field រួមគ្រប់ Entity — {@code id}, {@code isDeleted}, {@code createdAt}, {@code updatedAt}។
 *
 * <p>{@code @MappedSuperclass} មិនបង្កើតតារាងទេ — column ទាំង ៤ ចម្លងចូលតារាងកូនម្នាក់ៗ។
 *
 * <p>{@code createdAt} និង {@code updatedAt} បំពេញដោយ Spring Data Auditing ដែលបើកនៅ
 * {@code config/JpaAuditingConfig}។ បើ config នោះបាត់ តម្លៃទាំងពីរនឹង null ជានិច្ចដោយគ្មានកំហុសបង្ហាញ។
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /** សោចម្បង — auto-increment ដោយ database ហើយបង្ហាញលើ API ដោយផ្ទាល់។ */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Soft delete — គ្រប់ query ត្រូវច្រោះ {@code isDeleted = false}។ */
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /** សុវត្ថិភាពបន្ថែម៖ បំពេញ {@code isDeleted} បើ Service ភ្លេចកំណត់។ */
    @PrePersist
    protected void onPrePersist() {
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
}
