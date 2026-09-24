package co.panha.hibernate.tourmanagement.features.category;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CategoryResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CreateCategoryRequest;
import co.panha.hibernate.tourmanagement.features.category.dto.UpdateCategoryRequest;

/**
 * សេវាកម្មប្រភេទ Tour — F1។
 */
public interface CategoryService {

    /** UC1.1 — បង្កើតប្រភេទថ្មី។ */
    CategoryResponse createNew(CreateCategoryRequest request);

    /** UC1.2 — បញ្ជីប្រភេទ តម្រៀបតាមឈ្មោះ ក→អ។ */
    PageResponse<CategoryResponse> findAll(Integer page, Integer size);

    /** UC1.3 — ប្រភេទមួយតាម id។ */
    CategoryResponse findById(Long id);

    /**
     * UC1.4 — កែប្រភេទ (PATCH)។
     *
     * <p>បើ {@code name} ប្តូរ នោះ {@code slug} បង្កើតថ្មីតាម។ URL មិនរងផលប៉ះពាល់ទេ
     * ព្រោះវាប្រើ {@code id}។
     */
    CategoryResponse updateById(Long id, UpdateCategoryRequest request);

    /** UC1.5 — លុបប្រភេទ (soft delete)។ */
    void deleteById(Long id);
}
