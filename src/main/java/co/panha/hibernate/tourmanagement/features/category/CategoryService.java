package co.panha.hibernate.tourmanagement.features.category;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CategoryResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CreateCategoryRequest;
import co.panha.hibernate.tourmanagement.features.category.dto.UpdateCategoryRequest;

// Tour category service (F1).
public interface CategoryService {

    // UC1.1 - create a category.
    CategoryResponse createNew(CreateCategoryRequest request);

    // UC1.2 - list categories sorted by name.
    PageResponse<CategoryResponse> findAll(Integer page, Integer size);

    // UC1.3 - get one category by id.
    CategoryResponse findById(Long id);

    // UC1.4 - PATCH; slug is regenerated when name changes (URLs use id, so they are unaffected).
    CategoryResponse updateById(Long id, UpdateCategoryRequest request);

    // UC1.5 - soft delete a category.
    void deleteById(Long id);
}
