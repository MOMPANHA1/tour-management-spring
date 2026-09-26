package co.panha.hibernate.tourmanagement.features.review;

import co.panha.hibernate.tourmanagement.base.PageMapper;
import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.booking.Booking;
import co.panha.hibernate.tourmanagement.features.booking.BookingRepository;
import co.panha.hibernate.tourmanagement.features.booking.BookingStatus;
import co.panha.hibernate.tourmanagement.features.review.dto.CreateReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.HideReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReplyReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewSummaryResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.UpdateReviewRequest;
import co.panha.hibernate.tourmanagement.features.tour.Tour;
import co.panha.hibernate.tourmanagement.features.tour.TourRepository;
import co.panha.hibernate.tourmanagement.security.AuthUtils;
import co.panha.hibernate.tourmanagement.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    /** BR5 — វាយតម្លៃបានក្នុង ៦០ ថ្ងៃបន្ទាប់ពីត្រឡប់។ */
    private static final int REVIEW_WINDOW_DAYS = 60;

    /** BR6 — កែបានក្នុង ៧ ថ្ងៃបន្ទាប់ពីបង្កើត។ */
    private static final int EDIT_WINDOW_DAYS = 7;

    private static final int MIN_STAR = 1;
    private static final int MAX_STAR = 5;

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final ReviewMapper reviewMapper;

    /**
     * UC9.1 — ផ្តល់ការវាយតម្លៃ។
     *
     * <p>{@code tour} និង {@code customer} ទាញចេញពីការកក់ មិនមែនពី request ទេ —
     * ដូច្នេះអតិថិជនមិនអាចវាយតម្លៃ Tour ដែលខ្លួនមិនធ្លាប់ទៅបានឡើយ។
     */
    @Override
    @Transactional
    public ReviewResponse createNew(String bookingCode, CreateReviewRequest request) {

        // ២. Load
        Booking booking = loadOwnBooking(bookingCode);

        // ៣. Check Rules
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You can review only after the tour is completed (current: "
                            + booking.getStatus() + ")");
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already reviewed this booking");
        }

        long daysSinceReturn = DateUtils.daysBetween(
                booking.getSchedule().getReturnDate(), LocalDate.now());

        if (daysSinceReturn > REVIEW_WINDOW_DAYS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The review period has expired (allowed within " + REVIEW_WINDOW_DAYS
                            + " days of return)");
        }

        // ៥. Build
        Review review = reviewMapper.toEntity(request);
        review.setBooking(booking);
        review.setTour(booking.getSchedule().getTour());
        review.setCustomer(booking.getCustomer());
        review.setIsVisible(true);
        review.setIsDeleted(false);

        // ៦. Save
        Review saved = reviewRepository.saveAndFlush(review);

        // ៧. Side Effects
        recalculateTourRating(saved.getTour());

        // TODO ដំណាក់កាល ៥៖ notificationService.notifyNewReview(saved)
        log.info("Review {} created for tour {} by customer {} — {} star(s)",
                saved.getId(), saved.getTour().getId(), saved.getCustomer().getId(), saved.getRating());

        return toResponse(saved);
    }

    @Override
    public PageResponse<ReviewResponse> findByTour(Long tourId, Integer page, Integer size) {

        Tour tour = loadTour(tourId);
        Pageable pageable = PageMapper.buildPageable(page, size, "createdAt", Sort.Direction.DESC);

        Page<Review> result = reviewRepository.findVisibleByTour(tour.getId(), pageable);

        return PageMapper.toPageResponse(result, this::toResponse);
    }

    /**
     * UC9.7 — សង្ខេបពិន្ទុ។
     *
     * <p>បំពេញគ្រប់ផ្កាយ ១..៥ ជានិច្ច ទោះគ្មានការវាយតម្លៃក៏ដោយ — បើមិនដូច្នេះទេ
     * frontend ត្រូវពិនិត្យ {@code null} មុនគូររបារនីមួយៗ។
     */
    @Override
    public ReviewSummaryResponse getSummary(Long tourId) {

        Tour tour = loadTour(tourId);

        Map<Integer, Long> rawCounts = new LinkedHashMap<>();
        long total = 0;

        for (Object[] row : reviewRepository.countByStars(tour.getId())) {
            int star = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            rawCounts.put(star, count);
            total += count;
        }

        Map<Integer, Long> starCounts = new LinkedHashMap<>();
        Map<Integer, Double> starPercentages = new LinkedHashMap<>();

        for (int star = MAX_STAR; star >= MIN_STAR; star--) {
            long count = rawCounts.getOrDefault(star, 0L);
            starCounts.put(star, count);
            starPercentages.put(star, (total == 0) ? 0.0 : round1(count * 100.0 / total));
        }

        return new ReviewSummaryResponse(
                tour.getId(),
                round1(reviewRepository.averageRating(tour.getId())),
                total,
                round1(reviewRepository.averageGuideRating(tour.getId())),
                round1(reviewRepository.averageValueRating(tour.getId())),
                starCounts,
                starPercentages
        );
    }

    @Override
    @Transactional
    public ReviewResponse updateById(Long id, UpdateReviewRequest request) {

        Review review = loadOwnReview(id);

        if (Boolean.FALSE.equals(review.getIsVisible())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This review has been hidden by an administrator");
        }

        long daysSinceCreated = DateUtils.daysBetween(
                review.getCreatedAt().toLocalDate(), LocalDate.now());

        if (daysSinceCreated > EDIT_WINDOW_DAYS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A review can be edited only within " + EDIT_WINDOW_DAYS + " days of posting");
        }

        boolean ratingChanged = request.rating() != null
                && !request.rating().equals(review.getRating());

        reviewMapper.updateEntity(request, review);
        Review saved = reviewRepository.saveAndFlush(review);

        if (ratingChanged) {
            recalculateTourRating(saved.getTour());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        Review review = loadOwnReview(id);

        review.setIsDeleted(true);
        reviewRepository.saveAndFlush(review);

        recalculateTourRating(review.getTour());
    }

    @Override
    @Transactional
    public ReviewResponse reply(Long id, ReplyReviewRequest request) {

        Review review = loadById(id);

        if (review.getAdminReply() != null && !review.getAdminReply().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This review already has a reply — use PATCH to change it");
        }

        review.setAdminReply(request.reply());
        review.setRepliedAt(LocalDateTime.now());
        review.setRepliedBy(AuthUtils.currentUsername());
        reviewRepository.save(review);

        // TODO ដំណាក់កាល ៥៖ notificationService.notifyReviewReplied(review)

        return toResponse(review);
    }

    /**
     * UC9.6 — លាក់ការវាយតម្លៃមិនសមរម្យ។
     *
     * <p>ត្រូវគណនាពិន្ទុមធ្យមឡើងវិញ — ការវាយតម្លៃដែលលាក់មិនរាប់ចូលទេ។
     */
    @Override
    @Transactional
    public ReviewResponse hide(Long id, HideReviewRequest request) {

        Review review = loadById(id);

        if (Boolean.FALSE.equals(review.getIsVisible())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This review is already hidden");
        }

        review.setIsVisible(false);
        review.setHiddenReason(request.reason());
        reviewRepository.saveAndFlush(review);

        recalculateTourRating(review.getTour());

        log.info("Review {} hidden by {} — {}", review.getId(), AuthUtils.currentUsername(),
                request.reason());

        return toResponse(review);
    }

    // ---------- ជំនួយខាងក្នុង ----------

    /**
     * គណនា {@code tour.averageRating} និង {@code tour.reviewCount} ឡើងវិញ។
     *
     * <p>គណនាពីដើមជានិច្ច មិនមែនកែតម្លៃចាស់ទេ — ដូច្នេះការលាក់ ការលុប និងការកែពិន្ទុ
     * ទាំងអស់ផ្តល់លទ្ធផលត្រឹមត្រូវដោយតក្កវិជ្ជាតែមួយ។
     */
    private void recalculateTourRating(Tour tour) {

        tour.setAverageRating(round1(reviewRepository.averageRating(tour.getId())));
        tour.setReviewCount((int) reviewRepository.countVisibleByTour(tour.getId()));

        tourRepository.save(tour);
    }

    private Tour loadTour(Long tourId) {
        return tourRepository.findByIdAndIsDeletedFalse(tourId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tour not found with id = " + tourId));
    }

    private Review loadById(Long id) {
        return reviewRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Review not found with id = " + id));
    }

    /** ការវាយតម្លៃរបស់ខ្លួនឯង — បោះ <b>404</b> មិនមែន 403 ទេ ដូចលំនាំ {@code BookingServiceImpl}។ */
    private Review loadOwnReview(Long id) {

        Review review = loadById(id);

        if (!review.getCustomer().getKeycloakId().equals(AuthUtils.currentKeycloakId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found with id = " + id);
        }

        return review;
    }

    private Booking loadOwnBooking(String bookingCode) {

        Booking booking = bookingRepository.findByCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found with code = " + bookingCode));

        if (!booking.getCustomer().getKeycloakId().equals(AuthUtils.currentKeycloakId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Booking not found with code = " + bookingCode);
        }

        return booking;
    }

    /**
     * បិទបាំងឈ្មោះអតិថិជន — {@code "Sok Dara"} → {@code "Sok D***"}។
     *
     * <p>ការវាយតម្លៃជាទិន្នន័យសាធារណៈ ដូច្នេះមិនបង្ហាញឈ្មោះពេញទេ។ រក្សាពាក្យទីមួយ
     * និងអក្សរទីមួយនៃពាក្យបន្ទាប់ ដើម្បីឲ្យអ្នកអានសម្គាល់បានថាជាមនុស្សផ្សេងគ្នា។
     */
    private String maskCustomerName(String fullName) {

        if (fullName == null || fullName.isBlank()) {
            return "Anonymous";
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            String name = parts[0];
            return (name.length() <= 1) ? name + "***" : name.charAt(0) + "***";
        }

        return parts[0] + " " + parts[1].charAt(0) + "***";
    }

    private ReviewResponse toResponse(Review review) {
        return reviewMapper.toResponse(review, maskCustomerName(review.getCustomer().getFullName()));
    }

    /** បង្គត់ទៅ ១ ខ្ទង់ទសភាគ — {@code 4.6666} → {@code 4.7}។ */
    private Double round1(Double value) {
        if (value == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
