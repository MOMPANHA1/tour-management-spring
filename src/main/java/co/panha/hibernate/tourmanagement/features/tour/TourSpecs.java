package co.panha.hibernate.tourmanagement.features.tour;

import co.panha.hibernate.tourmanagement.features.destination.Destination;
import co.panha.hibernate.tourmanagement.features.schedule.ScheduleStatus;
import co.panha.hibernate.tourmanagement.features.schedule.TourSchedule;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

/**
 * លក្ខខណ្ឌស្វែងរក Tour — ផ្សំគ្នាបានតាមតម្រូវការ (UC4.2)។
 *
 * <p>ហេតុអ្វីប្រើ Specification ជំនួស {@code @Query}? ព្រោះ filter មាន ៨ លក្ខខណ្ឌជាជម្រើស —
 * ការសរសេរ JPQL គ្របដណ្តប់គ្រប់បន្សំត្រូវការ query ២⁸ = ២៥៦។ Specification ផ្សំវាពេល runtime។
 */
public final class TourSpecs {

    private TourSpecs() {
        // ថ្នាក់ឧបករណ៍ — មិនបង្កើត instance
    }

    public static Specification<Tour> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Tour> isPublished() {
        return (root, query, cb) -> cb.isTrue(root.get("isPublished"));
    }

    /** ស្វែងរកក្នុង {@code title} ឬ {@code description} ដោយមិនរើសអក្សរតូច/ធំ។ */
    public static Specification<Tour> titleOrDescLike(String keyword) {
        String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";

        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<Tour> hasCategory(String categoryUuid) {
        return (root, query, cb) -> cb.equal(root.get("category").get("uuid"), categoryUuid);
    }

    /**
     * Tour ដែលទៅដល់ទីតាំងនេះ។
     *
     * <p>{@code query.distinct(true)} ចាំបាច់ — ការ join តារាង M:N បង្កើតជួរស្ទួន
     * ពេល Tour មួយភ្ជាប់ទីតាំងច្រើន ហើយ {@code totalElements} នឹងរាប់លើស។
     */
    public static Specification<Tour> hasDestination(String destinationUuid) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            Join<Tour, Destination> destinations = root.join("destinations");
            return cb.equal(destinations.get("uuid"), destinationUuid);
        };
    }

    /** ជួរតម្លៃ — ព្រំដែនណាដែល null នឹងមិនកំណត់។ */
    public static Specification<Tour> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("price"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), min);
            }
            return cb.lessThanOrEqualTo(root.get("price"), max);
        };
    }

    /** ជួររយៈពេលជាថ្ងៃ — ព្រំដែនណាដែល null នឹងមិនកំណត់។ */
    public static Specification<Tour> daysBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("durationDays"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("durationDays"), min);
            }
            return cb.lessThanOrEqualTo(root.get("durationDays"), max);
        };
    }

    public static Specification<Tour> hasDifficulty(Difficulty difficulty) {
        return (root, query, cb) -> cb.equal(root.get("difficulty"), difficulty);
    }

    /**
     * Tour ដែលមានកាលវិភាគបើកទទួលការកក់ចេញដំណើរនៅ ឬក្រោយថ្ងៃដែលកំណត់។
     *
     * <p>ប្រើ {@code EXISTS} subquery មិនមែន {@code JOIN} ទេ — Tour មួយអាចមានកាលវិភាគរាប់រយ
     * ហើយ join នឹងបង្កើតជួរស្ទួនដែលត្រូវ {@code DISTINCT} ចេញវិញ។
     */
    public static Specification<Tour> hasScheduleAfter(LocalDate date) {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<TourSchedule> schedule = sub.from(TourSchedule.class);

            sub.select(schedule.get("id"))
                    .where(
                            cb.equal(schedule.get("tour"), root),
                            cb.isFalse(schedule.get("isDeleted")),
                            cb.greaterThanOrEqualTo(schedule.get("departureDate"), date),
                            schedule.get("status").in(ScheduleStatus.OPEN, ScheduleStatus.FULL)
                    );

            return cb.exists(sub);
        };
    }
}
