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

// Composable tour search conditions (UC4.2); Specification avoids writing a query per filter combination.
public final class TourSpecs {

    private TourSpecs() {
        // Utility class; no instances
    }

    public static Specification<Tour> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Tour> isPublished() {
        return (root, query, cb) -> cb.isTrue(root.get("isPublished"));
    }

    // Case-insensitive match on title or description.
    public static Specification<Tour> titleOrDescLike(String keyword) {
        String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";

        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<Tour> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    // Tours reaching this destination; distinct is needed because the M:N join duplicates rows and inflates totalElements.
    public static Specification<Tour> hasDestination(Long destinationId) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            Join<Tour, Destination> destinations = root.join("destinations");
            return cb.equal(destinations.get("id"), destinationId);
        };
    }

    // Price range; a null bound is unbounded.
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

    // Duration range in days; a null bound is unbounded.
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

    // Tours with an open schedule departing on or after the date; EXISTS avoids duplicate rows from a JOIN.
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
