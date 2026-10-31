package co.panha.hibernate.tourmanagement.base;

import java.util.List;

// Stable paged response shape; avoids exposing Spring Page internals and its version-dependent JSON.
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
) {
}
