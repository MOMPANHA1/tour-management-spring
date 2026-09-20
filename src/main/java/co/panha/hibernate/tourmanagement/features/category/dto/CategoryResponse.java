package co.panha.hibernate.tourmanagement.features.category.dto;

/**
 * ទិន្នន័យប្រភេទដែលត្រឡប់ទៅ client។
 *
 * <p>បង្ហាញ {@code uuid} មិនមែន {@code id} — សោខាងក្នុងមិនចេញក្រៅទេ។
 *
 * @param tourCount ចំនួន Tour សកម្ម (មិនរាប់ Tour ដែលលុប) ក្នុងប្រភេទនេះ។
 */
public record CategoryResponse(
        String uuid,
        String name,
        String slug,
        String description,
        String iconUrl,
        long tourCount
) {
}
