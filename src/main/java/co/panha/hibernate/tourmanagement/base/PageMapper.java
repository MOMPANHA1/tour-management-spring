package co.panha.hibernate.tourmanagement.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

/**
 * ឧបករណ៍រួមសម្រាប់ការបែងចែកទំព័រ — បម្លែង {@link Page} ទៅ {@link PageResponse}
 * និងបង្កើត {@link Pageable} ដែលមានព្រំដែនសុវត្ថិភាព។
 */
public final class PageMapper {

    /** ទំហំទំព័រលំនាំដើម នៅពេល client មិនបញ្ជាក់ ឬបញ្ជាក់តម្លៃមិនត្រឹមត្រូវ។ */
    public static final int DEFAULT_SIZE = 10;

    /** ទំហំទំព័រអតិបរមា — ការពារ client សុំ {@code size=100000} ដែលនឹងធ្វើឲ្យ database លិច។ */
    public static final int MAX_SIZE = 100;

    /** Field តម្រៀបលំនាំដើម នៅពេលមិនបញ្ជាក់ {@code sortBy}។ */
    public static final String DEFAULT_SORT_BY = "createdAt";

    private PageMapper() {
        // ថ្នាក់ឧបករណ៍ — មិនបង្កើត instance
    }

    /**
     * បម្លែង {@code Page<E>} (Entity) ទៅ {@code PageResponse<D>} (DTO)។
     *
     * @param page   លទ្ធផលពី repository
     * @param mapper មុខងារបម្លែង Entity មួយទៅ DTO មួយ (ជាទូទៅ {@code mapper::toResponse})
     */
    public static <E, D> PageResponse<D> toPageResponse(Page<E> page, Function<E, D> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    /** បម្លែង {@code Page} ដែលផ្ទុក DTO រួចស្រេច។ */
    public static <D> PageResponse<D> toPageResponse(Page<D> page) {
        return toPageResponse(page, Function.identity());
    }

    /**
     * បង្កើត {@link Pageable} ដោយកែតម្លៃដែលមិនសមហេតុផលជំនួសឲ្យបោះកំហុស។
     *
     * <p>ការសម្រេចចិត្តដោយចេតនា៖ {@code page = -5} ឬ {@code size = 999} មិនមែនជាកំហុសរបស់អ្នកប្រើទេ
     * (ជាញឹកញាប់មកពី URL ដែលចម្លងខុស) ដូច្នេះត្រឡប់ទំព័រទីមួយវិញល្អជាងបោះ 400។
     *
     * @param page      លេខទំព័រចាប់ពី 0 — តម្លៃអវិជ្ជមានប្តូរជា 0
     * @param size      ទំហំទំព័រ — តម្លៃក្រៅ 1..{@value #MAX_SIZE} ប្តូរជា {@value #DEFAULT_SIZE}
     * @param sortBy    ឈ្មោះ field — null ឬទទេប្តូរជា {@value #DEFAULT_SORT_BY}
     * @param direction ទិសតម្រៀប — null ប្តូរជា {@code DESC}
     */
    public static Pageable buildPageable(Integer page, Integer size, String sortBy, Sort.Direction direction) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size <= 0 || size > MAX_SIZE) ? DEFAULT_SIZE : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy;
        Sort.Direction safeDirection = (direction == null) ? Sort.Direction.DESC : direction;

        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSortBy));
    }

    /** ផ្លូវកាត់៖ តម្រៀបតាម {@value #DEFAULT_SORT_BY} ពីថ្មីទៅចាស់។ */
    public static Pageable buildPageable(Integer page, Integer size) {
        return buildPageable(page, size, DEFAULT_SORT_BY, Sort.Direction.DESC);
    }
}
