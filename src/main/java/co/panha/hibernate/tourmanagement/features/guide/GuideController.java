package co.panha.hibernate.tourmanagement.features.guide;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.CreateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.GuideResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideStatusRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// F3 - Guides; ADMIN-only rules are enforced in phase 5 (Spring Security).
@Tag(name = "Guide")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guides")
public class GuideController {

    private final GuideService guideService;

    // UC3.6 - guides available in a date range; kept next to /{id} for readability.
    @GetMapping("/available")
    public List<GuideResponse> findAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return guideService.findAvailable(startDate, endDate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuideResponse createNew(@Valid @RequestBody CreateGuideRequest request) {
        return guideService.createNew(request);
    }

    @GetMapping
    public PageResponse<GuideResponse> findAll(
            @RequestParam(required = false) GuideStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return guideService.findAll(status, page, size);
    }

    @GetMapping("/{id}")
    public GuideResponse findById(@PathVariable Long id) {
        return guideService.findById(id);
    }

    @PatchMapping("/{id}")
    public GuideResponse updateById(@PathVariable Long id,
                                      @Valid @RequestBody UpdateGuideRequest request) {
        return guideService.updateById(id, request);
    }

    @PatchMapping("/{id}/status")
    public GuideResponse changeStatus(@PathVariable Long id,
                                      @Valid @RequestBody UpdateGuideStatusRequest request) {
        return guideService.changeStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        guideService.deleteById(id);
    }
}
