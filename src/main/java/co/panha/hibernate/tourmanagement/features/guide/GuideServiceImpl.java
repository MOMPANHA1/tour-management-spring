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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already exists");
        }

        if (request.email() != null && guideRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Guide guide = guideMapper.toEntity(request);
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate and endDate are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be on or after startDate");
        }

        // ទាញបញ្ជីអ្នករវល់ជាមុន ជំនួសឲ្យ subquery NOT IN — អានងាយជាង ហើយបញ្ជីមគ្គុទ្ទេសក៍តូច
        List<Long> busyIds = scheduleRepository.findBusyGuideIds(startDate, endDate);

        List<Guide> available = busyIds.isEmpty()
                ? guideRepository.findAllByStatusAndIsDeletedFalse(GuideStatus.ACTIVE)
                : guideRepository.findAllByStatusAndIsDeletedFalseAndIdNotIn(GuideStatus.ACTIVE, busyIds);

        return available.stream().map(this::toResponse).toList();
    }

    @Override
    public GuideResponse findById(Long id) {
        return toResponse(loadById(id));
    }

    @Override
    @Transactional
    public GuideResponse updateById(Long id, UpdateGuideRequest request) {

        Guide guide = loadById(id);

        if (request.phoneNumber() != null
                && !request.phoneNumber().equals(guide.getPhoneNumber())
                && guideRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already exists");
        }

        if (request.email() != null
                && !request.email().equalsIgnoreCase(guide.getEmail())
                && guideRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        guideMapper.updateEntity(request, guide);

        return toResponse(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public GuideResponse changeStatus(Long id, UpdateGuideStatusRequest request) {

        Guide guide = loadById(id);

        if (guide.getStatus() == request.status()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Guide is already in status " + request.status());
        }

        // វិន័យ៖ ដាក់ INACTIVE មិនបានបើនៅមានកាលវិភាគអនាគត — ភ្ញៀវនឹងគ្មានមគ្គុទ្ទេសក៍
        if (request.status() == GuideStatus.INACTIVE) {
            long upcoming = scheduleRepository.countUpcomingByGuide(guide.getId(), LocalDate.now());

            if (upcoming > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Guide still has " + upcoming + " upcoming schedule(s) — reassign them first");
            }
        }

        guide.setStatus(request.status());

        return toResponse(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Guide guide = loadById(id);
        guide.setIsDeleted(true);
        guideRepository.save(guide);
    }

    // ---------- ជំនួយខាងក្នុង ----------

    private Guide loadById(Long id) {
        return guideRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Guide not found with id = " + id));
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
