package co.panha.hibernate.tourmanagement.features.guide;

import co.panha.hibernate.tourmanagement.base.PageMapper;
import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.CreateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.GuideResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideStatusRequest;
import co.panha.hibernate.tourmanagement.features.schedule.ScheduleRepository;
import co.panha.hibernate.tourmanagement.utils.GenerateUtils;
import co.panha.hibernate.tourmanagement.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuideServiceImpl implements GuideService {

    private static final String CODE_PREFIX = "GD";

    private final GuideRepository guideRepository;
    private final ScheduleRepository scheduleRepository;
    private final GuideMapper guideMapper;

    @Override
    @Transactional
    public GuideResponse createNew(CreateGuideRequest request) {

        if (guideRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "លេខទូរស័ព្ទនេះមានក្នុងប្រព័ន្ធរួចហើយ");
        }

        if (request.email() != null && guideRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "អ៊ីមែលនេះមានក្នុងប្រព័ន្ធរួចហើយ");
        }

        Guide guide = guideMapper.toEntity(request);
        guide.setUuid(GenerateUtils.randomUUID());
        guide.setCode(nextGuideCode());
        guide.setStatus(GuideStatus.ACTIVE);
        guide.setIsDeleted(false);

        return toResponse(guideRepository.save(guide));
    }

    @Override
    public PageResponse<GuideResponse> findAll(GuideStatus status, Integer page, Integer size) {

        Pageable pageable = PageMapper.buildPageable(page, size, "fullName", Sort.Direction.ASC);

        Page<Guide> result = (status == null)
                ? guideRepository.findAllByIsDeletedFalse(pageable)
                : guideRepository.findAllByStatusAndIsDeletedFalse(status, pageable);

        return PageMapper.toPageResponse(result, this::toResponse);
    }

    @Override
    public List<GuideResponse> findAvailable(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ត្រូវបញ្ជាក់ startDate និង endDate");
        }

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate ត្រូវក្រោយ startDate");
        }

        // ទាញបញ្ជីអ្នករវល់ជាមុន ជំនួសឲ្យ subquery NOT IN — អានងាយជាង ហើយបញ្ជីមគ្គុទ្ទេសក៍តូច
        List<Long> busyIds = scheduleRepository.findBusyGuideIds(startDate, endDate);

        List<Guide> available = busyIds.isEmpty()
                ? guideRepository.findAllByStatusAndIsDeletedFalse(GuideStatus.ACTIVE)
                : guideRepository.findAllByStatusAndIsDeletedFalseAndIdNotIn(GuideStatus.ACTIVE, busyIds);

        return available.stream().map(this::toResponse).toList();
    }

    @Override
    public GuideResponse findByUuid(String uuid) {
        return toResponse(loadByUuid(uuid));
    }

    @Override
    @Transactional
    public GuideResponse updateByUuid(String uuid, UpdateGuideRequest request) {

        Guide guide = loadByUuid(uuid);

        if (request.phoneNumber() != null
                && !request.phoneNumber().equals(guide.getPhoneNumber())
                && guideRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "លេខទូរស័ព្ទនេះមានក្នុងប្រព័ន្ធរួចហើយ");
        }

        if (request.email() != null
                && !request.email().equalsIgnoreCase(guide.getEmail())
                && guideRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "អ៊ីមែលនេះមានក្នុងប្រព័ន្ធរួចហើយ");
        }

        guideMapper.updateEntity(request, guide);

        return toResponse(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public GuideResponse changeStatus(String uuid, UpdateGuideStatusRequest request) {

        Guide guide = loadByUuid(uuid);

        if (guide.getStatus() == request.status()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "មគ្គុទ្ទេសក៍នេះស្ថិតក្នុងស្ថានភាព " + request.status() + " រួចហើយ");
        }

        // វិន័យ៖ ដាក់ INACTIVE មិនបានបើនៅមានកាលវិភាគអនាគត — ភ្ញៀវនឹងគ្មានមគ្គុទ្ទេសក៍
        if (request.status() == GuideStatus.INACTIVE) {
            long upcoming = scheduleRepository.countUpcomingByGuide(guide.getId(), LocalDate.now());

            if (upcoming > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "នៅមានកាលវិភាគអនាគត " + upcoming + " ត្រូវប្តូរមគ្គុទ្ទេសក៍សិន");
            }
        }

        guide.setStatus(request.status());

        return toResponse(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public void deleteByUuid(String uuid) {
        Guide guide = loadByUuid(uuid);
        guide.setIsDeleted(true);
        guideRepository.save(guide);
    }

    // ---------- ជំនួយខាងក្នុង ----------

    private Guide loadByUuid(String uuid) {
        return guideRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "រកមិនឃើញមគ្គុទ្ទេសក៍ uuid = " + uuid));
    }

    /**
     * បង្កើតលេខកូដបន្ទាប់ដែលមិនស្ទួន។
     *
     * <p>{@code countByIsDeletedFalse()} តែម្នាក់ឯងមិនគ្រប់គ្រាន់ទេ៖ បង្កើត GD-0001 រួចលុបវា (soft)
     * នោះចំនួននៅតែ 0 ហើយការបង្កើតបន្ទាប់នឹងព្យាយាមប្រើ GD-0001 ម្តងទៀត → ប៉ះ unique constraint។
     * ដូច្នេះត្រូវរំកិលទៅមុខរហូតដល់លេខទំនេរ ដូចលំនាំ slug ដែរ។
     */
    /** បំពេញ {@code assignedScheduleCount} ដែលមាននៅ database មិនមែនក្នុង entity។ */
    private GuideResponse toResponse(Guide guide) {
        return guideMapper.toResponse(guide,
                scheduleRepository.countByGuideIdAndIsDeletedFalse(guide.getId()));
    }

    private String nextGuideCode() {
        long count = guideRepository.countByIsDeletedFalse();

        String code = GenerateUtils.generateSequentialCode(CODE_PREFIX, count);
        while (guideRepository.existsByCode(code)) {
            count++;
            code = GenerateUtils.generateSequentialCode(CODE_PREFIX, count);
        }
        return code;
    }
}
