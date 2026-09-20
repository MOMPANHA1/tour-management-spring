package co.panha.hibernate.tourmanagement.base;

import java.util.List;

/**
 * រូបរាងឆ្លើយតបរួមសម្រាប់គ្រប់បញ្ជីដែលបែងចែកជាទំព័រ។
 *
 * <p>ហេតុអ្វីមិនត្រឡប់ {@code Page<T>} របស់ Spring ផ្ទាល់? ព្រោះ {@code Page} ផ្ទុក field ខាងក្នុងច្រើន
 * ({@code pageable}, {@code sort}, {@code numberOfElements}...) ដែល client មិនត្រូវការ ហើយរូបរាង JSON
 * របស់វាអាចប្រែប្រួលតាម version របស់ Spring។
 *
 * @param <T> ប្រភេទ DTO ក្នុងបញ្ជី
 */
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
