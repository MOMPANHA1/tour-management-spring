package co.panha.hibernate.tourmanagement.features.destination;

import co.panha.hibernate.tourmanagement.base.PageMapper;
import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.CreateDestinationRequest;
import co.panha.hibernate.tourmanagement.features.destination.dto.DestinationResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.UpdateDestinationRequest;
import co.panha.hibernate.tourmanagement.features.tour.TourRepository;
import co.panha.hibernate.tourmanagement.utils.GenerateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationServiceImpl implements DestinationService {

    private static final String DEFAULT_COUNTRY = "Cambodia";

    private final DestinationRepository destinationRepository;
    private final TourRepository tourRepository;
    private final DestinationMapper destinationMapper;

    @Override
    @Transactional
    public DestinationResponse createNew(CreateDestinationRequest request) {

        if (destinationRepository.existsByNameIgnoreCaseAndProvinceIgnoreCase(request.name(), request.province())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Destination '" + request.name() + "' in province " + request.province() + " already exists");
        }

        requireCoordinatePair(request.latitude(), request.longitude());

        Destination destination = destinationMapper.toEntity(request);
        destination.setCountry(defaultCountry(request.country()));
        destination.setIsDeleted(false);

        return toResponse(destinationRepository.save(destination));
    }

    @Override
    public PageResponse<DestinationResponse> findAll(String province, Integer page, Integer size) {

        Pageable pageable = PageMapper.buildPageable(page, size, "name", Sort.Direction.ASC);

        Page<Destination> result = (province == null || province.isBlank())
                ? destinationRepository.findAllByIsDeletedFalse(pageable)
                : destinationRepository.findAllByProvinceIgnoreCaseAndIsDeletedFalse(province.trim(), pageable);

        return PageMapper.toPageResponse(result, this::toResponse);
    }

    @Override
    public DestinationResponse findById(Long id) {
        // TODO: phase 3 (F4 Tour) - switch to DestinationDetailResponse carrying tours: List<TourCardResponse>
        return toResponse(loadById(id));
    }

    @Override
    @Transactional
    public DestinationResponse updateById(Long id, UpdateDestinationRequest request) {

        Destination destination = loadById(id);

        // Duplicate check only when name or province really changed
        String newName = (request.name() != null) ? request.name() : destination.getName();
        String newProvince = (request.province() != null) ? request.province() : destination.getProvince();

        boolean identityChanged = !newName.equalsIgnoreCase(destination.getName())
                || !newProvince.equalsIgnoreCase(destination.getProvince());

        if (identityChanged
                && destinationRepository.existsByNameIgnoreCaseAndProvinceIgnoreCase(newName, newProvince)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Destination '" + newName + "' in province " + newProvince + " already exists");
        }

        destinationMapper.updateEntity(request, destination);

        // Validate the final state, not the request: a PATCH may send only latitude onto a row without longitude.
        requireCoordinatePair(destination.getLatitude(), destination.getLongitude());

        return toResponse(destinationRepository.save(destination));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        Destination destination = loadById(id);

        // Rule: cannot delete while tours are linked to this destination
        long activeTours = tourRepository.countActiveByDestination(destination.getId());
        if (activeTours > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete destination with linked tours (" + activeTours + ")");
        }

        destination.setIsDeleted(true);
        destinationRepository.save(destination);
    }

    // ---------- internal helpers ----------

    private Destination loadById(Long id) {
        return destinationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Destination not found with id = " + id));
    }

    // Coordinates must be both set or both empty; a single value is meaningless on a map.
    private void requireCoordinatePair(BigDecimal latitude, BigDecimal longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "latitude and longitude must both be provided or both be empty");
        }
    }

    private String defaultCountry(String country) {
        return (country == null || country.isBlank()) ? DEFAULT_COUNTRY : country.trim();
    }

    // Fills tourCount, same as CategoryServiceImpl.toResponse.
    private DestinationResponse toResponse(Destination destination) {
        return destinationMapper.toResponse(destination,
                tourRepository.countActiveByDestination(destination.getId()));
    }
}
