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
                    "ទីតាំង '" + request.name() + "' នៅខេត្ត " + request.province() + " មានរួចហើយ");
        }

        requireCoordinatePair(request.latitude(), request.longitude());

        Destination destination = destinationMapper.toEntity(request);
        destination.setUuid(GenerateUtils.randomUUID());
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
    public DestinationResponse findByUuid(String uuid) {
        // TODO ដំណាក់កាល ៣ (F4 Tour)៖ ប្តូរទៅ DestinationDetailResponse ដែលផ្ទុក tours : List<TourCardResponse>
        return toResponse(loadByUuid(uuid));
    }

    @Override
    @Transactional
    public DestinationResponse updateByUuid(String uuid, UpdateDestinationRequest request) {

        Destination destination = loadByUuid(uuid);

        // ពិនិត្យស្ទួន — តែបើ name ឬ province ពិតជាប្តូរ
        String newName = (request.name() != null) ? request.name() : destination.getName();
        String newProvince = (request.province() != null) ? request.province() : destination.getProvince();

        boolean identityChanged = !newName.equalsIgnoreCase(destination.getName())
                || !newProvince.equalsIgnoreCase(destination.getProvince());

        if (identityChanged
                && destinationRepository.existsByNameIgnoreCaseAndProvinceIgnoreCase(newName, newProvince)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ទីតាំង '" + newName + "' នៅខេត្ត " + newProvince + " មានរួចហើយ");
        }

        destinationMapper.updateEntity(request, destination);

        // ពិនិត្យលើ​ស្ថានភាព​ចុងក្រោយ មិនមែនលើ request ទេ — ព្រោះ PATCH អាចផ្ញើមកតែ latitude
        // ហើយបន្សល់ទុកជួរដែលមាន latitude តែគ្មាន longitude។
        requireCoordinatePair(destination.getLatitude(), destination.getLongitude());

        return toResponse(destinationRepository.save(destination));
    }

    @Override
    @Transactional
    public void deleteByUuid(String uuid) {

        Destination destination = loadByUuid(uuid);

        // វិន័យ៖ លុបមិនបានបើនៅមាន Tour ភ្ជាប់នឹងទីតាំងនេះ
        long activeTours = tourRepository.countActiveByDestination(destination.getId());
        if (activeTours > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "មិនអាចលុបបានទេ ព្រោះនៅមាន " + activeTours + " Tour ភ្ជាប់នឹងទីតាំងនេះ");
        }

        destination.setIsDeleted(true);
        destinationRepository.save(destination);
    }

    // ---------- ជំនួយខាងក្នុង ----------

    private Destination loadByUuid(String uuid) {
        return destinationRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "រកមិនឃើញទីតាំង uuid = " + uuid));
    }

    /** កូអរដោនេត្រូវមានទាំងគូ ឬទទេទាំងគូ — ចំណុចតែមួយគ្មានន័យលើផែនទី។ */
    private void requireCoordinatePair(BigDecimal latitude, BigDecimal longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ត្រូវបំពេញ latitude និង longitude ទាំងពីរ ឬទុកទទេទាំងពីរ");
        }
    }

    private String defaultCountry(String country) {
        return (country == null || country.isBlank()) ? DEFAULT_COUNTRY : country.trim();
    }

    /** បំពេញ {@code tourCount} — ដូច {@code CategoryServiceImpl.toResponse} ដែរ។ */
    private DestinationResponse toResponse(Destination destination) {
        return destinationMapper.toResponse(destination,
                tourRepository.countActiveByDestination(destination.getId()));
    }
}
