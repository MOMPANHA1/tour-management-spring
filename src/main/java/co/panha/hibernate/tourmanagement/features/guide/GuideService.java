package co.panha.hibernate.tourmanagement.features.guide;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.CreateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.GuideResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideStatusRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * សេវាកម្មមគ្គុទ្ទេសក៍ — F3។
 */
public interface GuideService {

    /** UC3.1 — បង្កើតមគ្គុទ្ទេសក៍ថ្មី។ */
    GuideResponse createNew(CreateGuideRequest request);

    /** UC3.2 — បញ្ជីមគ្គុទ្ទេសក៍ ត្រងតាមស្ថានភាពបាន។ */
    PageResponse<GuideResponse> findAll(GuideStatus status, Integer page, Integer size);

    /** UC3.6 — មគ្គុទ្ទេសក៍ទំនេរក្នុងចន្លោះកាលបរិច្ឆេទ។ */
    List<GuideResponse> findAvailable(LocalDate startDate, LocalDate endDate);

    /** UC3.3 — ប្រវត្តិរូបមគ្គុទ្ទេសក៍ម្នាក់។ */
    GuideResponse findById(Long id);

    /** UC3.4 — កែព័ត៌មាន (PATCH)។ */
    GuideResponse updateById(Long id, UpdateGuideRequest request);

    /** UC3.5 — ប្តូរស្ថានភាព។ */
    GuideResponse changeStatus(Long id, UpdateGuideStatusRequest request);

    /** លុបមគ្គុទ្ទេសក៍ (soft delete)។ */
    void deleteById(Long id);
}
