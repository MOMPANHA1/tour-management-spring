package co.panha.hibernate.tourmanagement.features.category;

import co.panha.hibernate.tourmanagement.base.PageMapper;
import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CategoryResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CreateCategoryRequest;
import co.panha.hibernate.tourmanagement.features.category.dto.UpdateCategoryRequest;
import co.panha.hibernate.tourmanagement.features.tour.TourRepository;
import co.panha.hibernate.tourmanagement.utils.GenerateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TourRepository tourRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createNew(CreateCategoryRequest request) {

        // 3. Check rules
        if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Category name '" + request.name() + "' already exists");
        }

        // 5. Build
        Category category = categoryMapper.toEntity(request);
        category.setSlug(GenerateUtils.generateUniqueSlug(
                request.name(), categoryRepository::existsBySlugAndIsDeletedFalse));
        category.setIsDeleted(false);

        // 6. Save + 8. Return
        return toResponse(categoryRepository.save(category));
    }

    @Override
    public PageResponse<CategoryResponse> findAll(Integer page, Integer size) {
        Pageable pageable = PageMapper.buildPageable(page, size, "name", Sort.Direction.ASC);
        Page<Category> result = categoryRepository.findAllByIsDeletedFalse(pageable);

        return PageMapper.toPageResponse(result, this::toResponse);
    }

    @Override
    public CategoryResponse findById(Long id) {
        return toResponse(loadById(id));
    }

    @Override
    @Transactional
    public CategoryResponse updateById(Long id, UpdateCategoryRequest request) {

        Category category = loadById(id);

        // Duplicate check only when the name really changed
        if (request.name() != null && !request.name().equalsIgnoreCase(category.getName())) {

            if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name '" + request.name() + "' already exists");
            }
            category.setSlug(GenerateUtils.generateUniqueSlug(
                    request.name(), categoryRepository::existsBySlugAndIsDeletedFalse));
        }

        categoryMapper.updateEntity(request, category);

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        Category category = loadById(id);

        // Rule: cannot delete while active tours exist
        long activeTours = tourRepository.countActiveByCategory(category.getId());
        if (activeTours > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete category with active tours (" + activeTours + ")");
        }

        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

    // Load a category by id or throw 404; shared by three methods.
    private Category loadById(Long id) {
        return categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Category not found with id = " + id));
    }

    // Fills tourCount from the database; one count query per row, so switch to GROUP BY if the list grows.
    private CategoryResponse toResponse(Category category) {
        return categoryMapper.toResponse(category, tourRepository.countActiveByCategory(category.getId()));
    }
}
