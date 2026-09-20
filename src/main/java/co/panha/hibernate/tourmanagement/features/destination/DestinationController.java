package co.panha.hibernate.tourmanagement.features.destination;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.CreateDestinationRequest;
import co.panha.hibernate.tourmanagement.features.destination.dto.DestinationResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.UpdateDestinationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/**
 * F2 — ទីតាំងគោលដៅ។
 *
 * <p>សិទ្ធិ (ADMIN សម្រាប់ POST/PATCH/DELETE) នឹងអនុវត្តនៅដំណាក់កាល ៥ ពេលដាក់ Spring Security។
 */
@Tag(name = "Destination", description = "ទីតាំងគោលដៅ")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DestinationResponse createNew(@Valid @RequestBody CreateDestinationRequest request) {
        return destinationService.createNew(request);
    }

    @GetMapping
    public PageResponse<DestinationResponse> findAll(
            @RequestParam(required = false) String province,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return destinationService.findAll(province, page, size);
    }

    @GetMapping("/{uuid}")
    public DestinationResponse findByUuid(@PathVariable String uuid) {
        return destinationService.findByUuid(uuid);
    }

    @PatchMapping("/{uuid}")
    public DestinationResponse updateByUuid(@PathVariable String uuid,
                                            @Valid @RequestBody UpdateDestinationRequest request) {
        return destinationService.updateByUuid(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUuid(@PathVariable String uuid) {
        destinationService.deleteByUuid(uuid);
    }
}
