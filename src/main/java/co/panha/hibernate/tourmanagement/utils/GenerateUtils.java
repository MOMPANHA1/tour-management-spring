package co.panha.hibernate.tourmanagement.utils;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Predicate;

// Generates business codes and slugs.
public final class GenerateUtils {

    private static final DateTimeFormatter DATE_CODE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    // Fallback slug when the title has no Latin characters.
    private static final String FALLBACK_SLUG = "item";

    private GenerateUtils() {
        // Utility class; no instances.
    }

    // Sequential code, e.g. GD-0001.
    public static String generateSequentialCode(String prefix, long currentCount) {
        return "%s-%04d".formatted(prefix, currentCount + 1);
    }

    // Date-based code, e.g. SC-20260912-07.
    public static String generateDateCode(String prefix, LocalDate date) {
        return "%s-%s-%02d".formatted(prefix, DATE_CODE_FORMAT.format(date), RANDOM.nextInt(100));
    }

    // Date plus daily sequence code, e.g. BK-20260912-0001.
    public static String generateDateSequentialCode(String prefix, LocalDate date, long todayCount) {
        return "%s-%s-%04d".formatted(prefix, DATE_CODE_FORMAT.format(date), todayCount + 1);
    }

    // Builds a unique slug by appending a number until free; Khmer-only titles fall back to the fallback slug.
    public static String generateUniqueSlug(String text, Predicate<String> existsChecker) {
        String base = toSlug(text);

        String slug = base;
        int suffix = 1;
        while (existsChecker.test(slug)) {
            slug = base + "-" + suffix;
            suffix++;
        }
        return slug;
    }

    // Converts text to a slug without checking uniqueness.
    public static String toSlug(String text) {
        if (text == null || text.isBlank()) {
            return FALLBACK_SLUG;
        }

        String slug = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")            // strip diacritics (e.g. é -> e)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")       // non-alphanumerics -> hyphen
                .replaceAll("^-+|-+$", "");          // trim leading/trailing hyphens

        return slug.isBlank() ? FALLBACK_SLUG : slug;
    }
}
