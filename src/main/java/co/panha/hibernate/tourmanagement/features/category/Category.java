package co.panha.hibernate.tourmanagement.features.category;

import co.panha.hibernate.tourmanagement.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ប្រភេទ Tour — ធម្មជាតិ · វប្បធម៌ · សមុទ្រ · ផ្សងព្រេង។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(unique = true, nullable = false, length = 80)
    private String name;

    /** បង្កើតពី {@code name} ដោយ {@code GenerateUtils.generateUniqueSlug} — ប្រើលើ URL។ */
    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String iconUrl;

    // ចំណាំ៖ គ្មាន @OneToMany ទៅ Tour ទេ — ការរាប់ធ្វើដោយ TourRepository.countActiveByCategory()
    //   ព្រោះការផ្ទុកបញ្ជី Tour ទាំងមូលចូលអង្គចងចាំគ្រាន់តែដើម្បីរាប់ វាខ្ជះខ្ជាយ។
}
