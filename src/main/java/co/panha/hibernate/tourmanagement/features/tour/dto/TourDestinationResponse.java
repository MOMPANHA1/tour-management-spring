package co.panha.hibernate.tourmanagement.features.tour.dto;

import java.math.BigDecimal;

/**
 * ទីតាំងដែលបង្ហាញក្នុងទំព័រលម្អិត Tour។
 *
 * <p>ហេតុអ្វីមិនប្រើ {@code DestinationResponse} ដដែល? ព្រោះវាមាន {@code tourCount} ដែល
 * <b>គ្មានន័យនៅទីនេះ</b> — អ្នកកំពុងមើល Tour មួយ មិនមែនរាប់ Tour ក្នុងទីតាំងទេ។ ការបំពេញវាដោយ 0
 * នឹងជាការកុហក ចំណែកការគណនាវានឹងបង្ក query បន្ថែមដោយឥតប្រយោជន៍។
 */
public record TourDestinationResponse(
        Long id,
        String name,
        String province,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        String imageUrl
) {
}
