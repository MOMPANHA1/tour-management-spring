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

        // ៣. Check Rules
        if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ប្រភេទឈ្មោះ '" + request.name() + "' មានរួចហើយ");
        }

        // ៥. Build
        Category category = categoryMapper.toEntity(request);
        category.setSlug(GenerateUtils.generateUniqueSlug(
                request.name(), categoryRepository::existsBySlugAndIsDeletedFalse));
        category.setIsDeleted(false);

        // ៦. Save + ៨. Return
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

        // ពិនិត្យស្ទួន — តែបើឈ្មោះពិតជាប្តូរ
        if (request.name() != null && !request.name().equalsIgnoreCase(category.getName())) {

            if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "ប្រភេទឈ្មោះនេះមានរួចហើយ");
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

        // វិន័យ៖ លុបមិនបានបើនៅមាន Tour សកម្ម
        long activeTours = tourRepository.countActiveByCategory(category.getId());
        if (activeTours > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "មិនអាចលុបបានទេ ព្រោះនៅមាន " + activeTours + " Tour ក្នុងប្រភេទនេះ");
        }

        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

    // ---------- ជំនួយខាងក្នុង ----------

    /** ទាញប្រភេទតាម id ឬបោះ 404 — ប្រើរួមគ្នាដោយ ៣ method។ */
    private Category loadById(Long id) {
        return categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "រកមិនឃើញប្រភេទ id = " + id));
    }

    /**
     * បំពេញ {@code tourCount} ដែលមាននៅក្នុង database មិនមែនក្នុង entity។
     *
     * <p><b>ចំណាំអំពីដំណើរការ</b>៖ នៅ {@code findAll} វាបង្កើត count query ១ ក្នុងមួយប្រភេទ
     * (ទំព័រ ១០ → ១១ query)។ ទទួលយកបានសម្រាប់តារាងប្រភេទដែលតូច (ជាធម្មតាក្រោម ២០ ជួរ)។
     * បើថ្ងៃក្រោយបញ្ជីនេះធំឡើង សូមប្តូរទៅ query រាប់តែមួយដែល {@code GROUP BY category_id}។
     */
    private CategoryResponse toResponse(Category category) {
        return categoryMapper.toResponse(category, tourRepository.countActiveByCategory(category.getId()));
    }
}
