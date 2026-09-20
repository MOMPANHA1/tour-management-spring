package co.panha.hibernate.tourmanagement.features.tour;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.CreateTourRequest;
import co.panha.hibernate.tourmanagement.features.tour.dto.PublishTourRequest;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourCardResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourDetailResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourFilter;
import co.panha.hibernate.tourmanagement.features.tour.dto.UpdateTourRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * F4 — កញ្ចប់ដំណើរកម្សាន្ត។
 *
 * <p>សិទ្ធិ (ADMIN សម្រាប់ POST/PATCH/DELETE) នឹងអនុវត្តនៅដំណាក់កាល ៥ ពេលដាក់ Spring Security។
 */
@Tag(name = "Tour", description = "កញ្ចប់ដំណើរកម្សាន្ត")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tours")
public class TourController {

    private final TourService tourService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TourDetailResponse createNew(@Valid @RequestBody CreateTourRequest request) {
        return tourService.createNew(request);
    }

    /**
     * UC4.2 — ស្វែងរក។
     *
     * <p>ប៉ារ៉ាម៉ែត្រសរសេរដាច់ៗជំនួសឲ្យ {@code @ModelAttribute TourFilter} ដើម្បីឲ្យ Swagger
     * បង្ហាញវាម្នាក់ៗ ហើយឲ្យការបម្លែងប្រភេទ (enum, date, decimal) ចេញជា 400 ច្បាស់លាស់។
     */
    @GetMapping
    public PageResponse<TourCardResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryUuid,
            @RequestParam(required = false) String destinationUuid,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minDays,
            @RequestParam(required = false) Integer maxDays,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureAfter,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {

        TourFilter filter = new TourFilter(keyword, categoryUuid, destinationUuid,
                minPrice, maxPrice, minDays, maxDays, difficulty, departureAfter, sortBy);

        return tourService.search(filter, page, size);
    }

    @GetMapping("/popular")
    public List<TourCardResponse> findPopular(@RequestParam(defaultValue = "10") Integer limit) {
        return tourService.findPopular(limit);
    }

    @GetMapping("/{uuid}")
    public TourDetailResponse findByUuid(@PathVariable String uuid) {
        return tourService.findByUuid(uuid);
    }

    @PatchMapping("/{uuid}")
    public TourDetailResponse updateByUuid(@PathVariable String uuid,
                                           @Valid @RequestBody UpdateTourRequest request) {
        return tourService.updateByUuid(uuid, request);
    }

    @PatchMapping("/{uuid}/publish")
    public TourDetailResponse publish(@PathVariable String uuid,
                                      @Valid @RequestBody PublishTourRequest request) {
        return tourService.publish(uuid, request.published());
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUuid(@PathVariable String uuid) {
        tourService.deleteByUuid(uuid);
    }
}
