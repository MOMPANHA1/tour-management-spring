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

    /** UC1.3 — ប្រភេទមួយតាម uuid។ */
    CategoryResponse findByUuid(String uuid);

    /** UC1.4 — កែប្រភេទ (PATCH)។ */
    CategoryResponse updateByUuid(String uuid, UpdateCategoryRequest request);

    /** UC1.5 — លុបប្រភេទ (soft delete)។ */
    void deleteByUuid(String uuid);
}
