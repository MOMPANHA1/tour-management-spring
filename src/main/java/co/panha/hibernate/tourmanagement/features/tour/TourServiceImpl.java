package co.panha.hibernate.tourmanagement.features.tour;

import co.panha.hibernate.tourmanagement.base.PageMapper;
import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.booking.BookingRepository;
import co.panha.hibernate.tourmanagement.features.category.Category;
import co.panha.hibernate.tourmanagement.features.category.CategoryRepository;
import co.panha.hibernate.tourmanagement.features.destination.Destination;
import co.panha.hibernate.tourmanagement.features.destination.DestinationRepository;
import co.panha.hibernate.tourmanagement.features.schedule.ScheduleRepository;
import co.panha.hibernate.tourmanagement.features.tour.dto.CreateTourRequest;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourCardResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourDetailResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourFilter;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourImageRequest;
import co.panha.hibernate.tourmanagement.features.tour.dto.UpdateTourRequest;
import co.panha.hibernate.tourmanagement.utils.GenerateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourServiceImpl implements TourService {

    private static final String CODE_PREFIX = "TR";
    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_POPULAR_LIMIT = 10;

    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;
    private final DestinationRepository destinationRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final TourMapper tourMapper;

    @Override
    @Transactional
    public TourDetailResponse createNew(CreateTourRequest request) {

        // 1. Validate cross-field rules
        requireNightsWithinDays(request.durationNights(), request.durationDays());
        requireGroupSizeOrder(request.minGroupSize(), request.maxGroupSize());

        // 2. Load related resources
        Category category = loadCategory(request.categoryId());
        Set<Destination> destinations = loadDestinations(request.destinationIds());

        // 3. Build
        Tour tour = tourMapper.toEntity(request);
        tour.setCode(nextTourCode());
        tour.setSlug(GenerateUtils.generateUniqueSlug(request.title(), tourRepository::existsBySlug));
        tour.setCategory(category);
        tour.setDestinations(destinations);
        tour.setIsPublished(false);          // Always created as draft
        tour.setAverageRating(0.0);
        tour.setReviewCount(0);
        tour.setIsDeleted(false);

        if (tour.getMinGroupSize() == null) {
            tour.setMinGroupSize(1);
        }

        replaceImages(tour, request.images());

        // 4. Save and return
        return toDetailResponse(tourRepository.save(tour));
    }

    @Override
    public PageResponse<TourCardResponse> search(TourFilter filter, Integer page, Integer size) {

        List<Specification<Tour>> specs = new ArrayList<>();
        specs.add(TourSpecs.isNotDeleted());
        specs.add(TourSpecs.isPublished());

        if (filter.keyword() != null && !filter.keyword().isBlank()) {
            specs.add(TourSpecs.titleOrDescLike(filter.keyword().trim()));
        }
        if (filter.categoryId() != null) {
            specs.add(TourSpecs.hasCategory(filter.categoryId()));
        }
        if (filter.destinationId() != null) {
            specs.add(TourSpecs.hasDestination(filter.destinationId()));
        }
        if (filter.minPrice() != null || filter.maxPrice() != null) {
            requirePriceOrder(filter.minPrice(), filter.maxPrice());
            specs.add(TourSpecs.priceBetween(filter.minPrice(), filter.maxPrice()));
        }
        if (filter.minDays() != null || filter.maxDays() != null) {
            specs.add(TourSpecs.daysBetween(filter.minDays(), filter.maxDays()));
        }
        if (filter.difficulty() != null) {
            specs.add(TourSpecs.hasDifficulty(filter.difficulty()));
        }

        if (filter.departureAfter() != null) {
            specs.add(TourSpecs.hasScheduleAfter(filter.departureAfter()));
        }

        Page<Tour> result = tourRepository.findAll(
                Specification.allOf(specs),
                PageRequest.of(safePage(page), safeSize(size), resolveSort(filter.sortBy()))
        );

        return PageMapper.toPageResponse(result, this::toCardResponse);
    }

    @Override
    public TourDetailResponse findById(Long id) {
        return toDetailResponse(loadById(id));
    }

    @Override
    @Transactional
    public TourDetailResponse updateById(Long id, UpdateTourRequest request) {

        Tour tour = loadById(id);

        // Validate the final result, not the request: a PATCH with only durationNights could leave nights > days.
        Integer finalDays = (request.durationDays() != null) ? request.durationDays() : tour.getDurationDays();
        Integer finalNights = (request.durationNights() != null) ? request.durationNights() : tour.getDurationNights();
        requireNightsWithinDays(finalNights, finalDays);

        Integer finalMin = (request.minGroupSize() != null) ? request.minGroupSize() : tour.getMinGroupSize();
        Integer finalMax = (request.maxGroupSize() != null) ? request.maxGroupSize() : tour.getMaxGroupSize();
        requireGroupSizeOrder(finalMin, finalMax);

        if (request.title() != null && !request.title().equals(tour.getTitle())) {
            tour.setSlug(GenerateUtils.generateUniqueSlug(request.title(), tourRepository::existsBySlug));
        }

        if (request.categoryId() != null) {
            tour.setCategory(loadCategory(request.categoryId()));
        }

        if (request.destinationIds() != null) {
            if (request.destinationIds().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one destination is required");
            }
            tour.setDestinations(loadDestinations(request.destinationIds()));
        }

        tourMapper.updateEntity(request, tour);

        // Image list is a full replacement; null means leave untouched
        if (request.images() != null) {
            replaceImages(tour, request.images());
        }

        return toDetailResponse(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public TourDetailResponse publish(Long id, boolean shouldPublish) {

        Tour tour = loadById(id);

        if (shouldPublish) {
            requireReadyToPublish(tour);
        }

        tour.setIsPublished(shouldPublish);

        return toDetailResponse(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        Tour tour = loadById(id);

        // Cannot delete while active bookings exist; customers have already paid
        long activeBookings = bookingRepository.countActiveBookingsByTour(tour.getId());
        if (activeBookings > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete tour with active bookings (" + activeBookings + ")");
        }

        tour.setIsPublished(false);          // Take off sale before deleting
        tour.setIsDeleted(true);
        tourRepository.save(tour);
    }

    @Override
    public List<TourCardResponse> findPopular(Integer limit) {
        int safeLimit = (limit == null || limit <= 0 || limit > MAX_PAGE_SIZE) ? DEFAULT_POPULAR_LIMIT : limit;

        return tourRepository.findPopular(PageRequest.of(0, safeLimit))
                .getContent().stream()
                .map(this::toCardResponse)
                .toList();
    }

    // ---------- Internal helpers ----------

    // Fill nextDepartureDate; costs one query per tour in search, so switch to a grouped query if it gets slow.
    private TourCardResponse toCardResponse(Tour tour) {
        return tourMapper.toCardResponse(tour, nextDepartureDate(tour));
    }

    private TourDetailResponse toDetailResponse(Tour tour) {
        // TODO: phase 4 - add upcomingSchedules (F5 exists, DTO must grow) and recentReviews (F9)
        return tourMapper.toDetailResponse(tour, nextDepartureDate(tour));
    }

    private LocalDate nextDepartureDate(Tour tour) {
        return scheduleRepository.findNextDepartureDate(tour.getId(), LocalDate.now());
    }

    private Tour loadById(Long id) {
        return tourRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tour not found with id = " + id));
    }

    private Category loadCategory(Long categoryId) {
        return categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Category not found with slug = " + categoryId));
    }

    // Load destinations by id; the 404 lists every missing id.
    private Set<Destination> loadDestinations(Set<Long> ids) {
        List<Destination> found = destinationRepository.findAllByIdInAndIsDeletedFalse(List.copyOf(ids));

        if (found.size() != ids.size()) {
            Set<Long> missing = new LinkedHashSet<>(ids);
            found.forEach(destination -> missing.remove(destination.getId()));

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinations not found: " + missing);
        }

        return new LinkedHashSet<>(found);
    }

    // Replace the whole image list; orphanRemoval deletes old rows.
    private void replaceImages(Tour tour, List<TourImageRequest> imageRequests) {
        tour.clearImages();

        if (imageRequests == null) {
            return;
        }

        imageRequests.stream()
                .map(tourMapper::toImageEntity)
                .forEach(tour::addImage);
    }

    private void requireNightsWithinDays(Integer nights, Integer days) {
        if (nights != null && days != null && nights > days) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationNights cannot exceed durationDays");
        }
    }

    private void requireGroupSizeOrder(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minGroupSize cannot exceed maxGroupSize");
        }
    }

    private void requirePriceOrder(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice cannot exceed maxPrice");
        }
    }

    // Publish rule: tours with incomplete info must not appear on public pages.
    private void requireReadyToPublish(Tour tour) {

        if (tour.getDescription() == null || tour.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Description is required before publishing");
        }

        if (tour.getThumbnailUrl() == null || tour.getThumbnailUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "A cover image is required before publishing");
        }

        if (tour.getDestinations().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "At least one destination is required");
        }

        long openSchedules = scheduleRepository.countOpenSchedules(tour.getId(), LocalDate.now());
        if (openSchedules == 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "At least one departure schedule is required before publishing");
        }
    }

    // Map sortBy to Sort using a whitelist only; raw values would allow any property and cause 500s.
    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sortBy) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating", "reviewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private int safePage(Integer page) {
        return (page == null || page < 0) ? 0 : page;
    }

    private int safeSize(Integer size) {
        return (size == null || size <= 0 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;
    }

    // Same as GuideServiceImpl.nextGuideCode(); advance until the code is unused.
    private String nextTourCode() {
        long count = tourRepository.countByIsDeletedFalse();

        String code = GenerateUtils.generateSequentialCode(CODE_PREFIX, count);
        while (tourRepository.existsByCode(code)) {
            count++;
            code = GenerateUtils.generateSequentialCode(CODE_PREFIX, count);
        }
        return code;
    }
}
