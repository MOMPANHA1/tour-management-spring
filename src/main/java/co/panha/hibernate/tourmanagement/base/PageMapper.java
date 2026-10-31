package co.panha.hibernate.tourmanagement.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

// Converts Page to PageResponse and builds Pageable with safe bounds.
public final class PageMapper {

    // Default page size when the client omits or sends an invalid value.
    public static final int DEFAULT_SIZE = 10;

    // Max page size; prevents clients from requesting huge pages.
    public static final int MAX_SIZE = 100;

    // Default sort field when sortBy is not given.
    public static final String DEFAULT_SORT_BY = "createdAt";

    private PageMapper() {
        // Utility class; no instances.
    }

    // Maps a Page of entities to a PageResponse of DTOs.
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

    // Wraps a Page that already contains DTOs.
    public static <D> PageResponse<D> toPageResponse(Page<D> page) {
        return toPageResponse(page, Function.identity());
    }

    // Clamps invalid page/size/sort values to defaults instead of throwing (bad URLs are not user errors).
    public static Pageable buildPageable(Integer page, Integer size, String sortBy, Sort.Direction direction) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size <= 0 || size > MAX_SIZE) ? DEFAULT_SIZE : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy;
        Sort.Direction safeDirection = (direction == null) ? Sort.Direction.DESC : direction;

        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSortBy));
    }

    // Shortcut: sort by the default field, newest first.
    public static Pageable buildPageable(Integer page, Integer size) {
        return buildPageable(page, size, DEFAULT_SORT_BY, Sort.Direction.DESC);
    }
}
