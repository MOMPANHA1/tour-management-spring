package co.panha.hibernate.tourmanagement.features.category;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CategoryResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CreateCategoryRequest;
import co.panha.hibernate.tourmanagement.features.category.dto.UpdateCategoryRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * F1 — ប្រភេទ Tour។
 *
 * <p>សិទ្ធិ (ADMIN សម្រាប់ POST/PATCH/DELETE) នឹងអនុវត្តនៅដំណាក់កាល ៥ ពេលដាក់ Spring Security។
 */
@Tag(name = "Category")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createNew(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.createNew(request);
    }

    @GetMapping
    public PageResponse<CategoryResponse> findAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return categoryService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PatchMapping("/{id}")
    public CategoryResponse updateById(@PathVariable Long id,
                                        @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.updateById(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        categoryService.deleteById(id);
    }
}
