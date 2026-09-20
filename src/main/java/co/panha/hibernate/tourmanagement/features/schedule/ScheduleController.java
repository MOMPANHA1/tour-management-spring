package co.panha.hibernate.tourmanagement.features.schedule;

import co.panha.hibernate.tourmanagement.features.schedule.dto.AssignGuideRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.CancelScheduleRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.CreateScheduleRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.ScheduleResponse;
import co.panha.hibernate.tourmanagement.features.schedule.dto.UpdateScheduleRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * F5 — កាលវិភាគចេញដំណើរ។
 *
 * <p>សិទ្ធិ (ADMIN លើកលែង {@code GET}) នឹងអនុវត្តនៅដំណាក់កាល ៥ ពេលដាក់ Spring Security។
 */
@Tag(name = "Schedule", description = "កាលវិភាគចេញដំណើរ")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createNew(@Valid @RequestBody CreateScheduleRequest request) {
        return scheduleService.createNew(request);
    }

    @GetMapping("/{uuid}")
    public ScheduleResponse findByUuid(@PathVariable String uuid) {
        return scheduleService.findByUuid(uuid);
    }

    @PatchMapping("/{uuid}")
    public ScheduleResponse updateByUuid(@PathVariable String uuid,
                                         @Valid @RequestBody UpdateScheduleRequest request) {
        return scheduleService.updateByUuid(uuid, request);
    }

    @PatchMapping("/{uuid}/guide")
    public ScheduleResponse assignGuide(@PathVariable String uuid,
                                        @Valid @RequestBody AssignGuideRequest request) {
        return scheduleService.assignGuide(uuid, request);
    }

    @PatchMapping("/{uuid}/cancel")
    public ScheduleResponse cancel(@PathVariable String uuid,
                                   @Valid @RequestBody CancelScheduleRequest request) {
        return scheduleService.cancel(uuid, request);
    }
}
