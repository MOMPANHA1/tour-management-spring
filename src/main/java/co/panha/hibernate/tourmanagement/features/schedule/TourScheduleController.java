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

// UC5.2: public schedules of a tour; separate controller because the base path differs.
@Tag(name = "Schedule")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tours/{tourId}/schedules")
public class TourScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public List<ScheduleResponse> findByTour(
            @PathVariable Long tourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        return scheduleService.findByTour(tourId, fromDate);
    }
}
