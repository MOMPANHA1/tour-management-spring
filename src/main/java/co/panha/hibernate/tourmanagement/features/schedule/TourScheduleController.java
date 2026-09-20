package co.panha.hibernate.tourmanagement.features.schedule;

import co.panha.hibernate.tourmanagement.features.schedule.dto.ScheduleResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * UC5.2 — កាលវិភាគរបស់ Tour មួយ (សាធារណៈ)។
 *
 * <p>ដាក់ដាច់ពី {@code ScheduleController} ព្រោះ base path ខុសគ្នា — វាជាធនធានរងរបស់ Tour។
 */
@Tag(name = "Schedule", description = "កាលវិភាគចេញដំណើរ")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tours/{tourUuid}/schedules")
public class TourScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public List<ScheduleResponse> findByTour(
            @PathVariable String tourUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        return scheduleService.findByTour(tourUuid, fromDate);
    }
}
