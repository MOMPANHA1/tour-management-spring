package co.panha.hibernate.tourmanagement.features.category.dto;

/**
 * ទិន្នន័យប្រភេទដែលត្រឡប់ទៅ client។
 *
 * <p>{@code id} គឺជាលេខ auto-increment របស់ database ហើយជាអាសយដ្ឋានលើ API។
 *
 * @param tourCount ចំនួន Tour សកម្ម (មិនរាប់ Tour ដែលលុប) ក្នុងប្រភេទនេះ។
 */
public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String iconUrl,
        long tourCount
) {
}
