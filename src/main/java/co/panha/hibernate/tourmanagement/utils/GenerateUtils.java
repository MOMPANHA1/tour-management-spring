package co.panha.hibernate.tourmanagement.utils;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * ការបង្កើតតម្លៃ — UUID, លេខកូដអាជីវកម្ម, slug។
 */
public final class GenerateUtils {

    private static final DateTimeFormatter DATE_CODE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Slug បម្រុង នៅពេលចំណងជើងគ្មានតួអក្សរឡាតាំងសោះ (ឧ. ចំណងជើងខ្មែរសុទ្ធ)។ */
    private static final String FALLBACK_SLUG = "item";

    private GenerateUtils() {
        // ថ្នាក់ឧបករណ៍ — មិនបង្កើត instance
    }

    /** សោសាធារណៈសម្រាប់ {@code BaseEntity.uuid}។ */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * លេខកូដតាមលំដាប់ — {@code GD-0001}, {@code TR-0042}។
     *
     * @param prefix       អក្សរនាំមុខ (ឧ. {@code "GD"})
     * @param currentCount ចំនួនជួរដែលមានស្រាប់ក្នុងតារាង
     */
    public static String generateSequentialCode(String prefix, long currentCount) {
        return "%s-%04d".formatted(prefix, currentCount + 1);
    }

    /**
     * លេខកូដតាមកាលបរិច្ឆេទ — {@code SC-20260912-07}។
     *
     * @param prefix អក្សរនាំមុខ (ឧ. {@code "SC"})
     * @param date   កាលបរិច្ឆេទដែលបញ្ចូលក្នុងកូដ
     */
    public static String generateDateCode(String prefix, LocalDate date) {
        return "%s-%s-%02d".formatted(prefix, DATE_CODE_FORMAT.format(date), RANDOM.nextInt(100));
    }

    /**
     * លេខកូដតាមកាលបរិច្ឆេទ + លំដាប់ — {@code BK-20260912-0001}។
     *
     * <p>ប្រើសម្រាប់ {@code Booking} និង {@code Payment} ដែលត្រូវការលេខតាមលំដាប់ក្នុងមួយថ្ងៃ។
     *
     * @param prefix     អក្សរនាំមុខ (ឧ. {@code "BK"})
     * @param date       កាលបរិច្ឆេទ
     * @param todayCount ចំនួនជួរដែលបង្កើតរួចក្នុងថ្ងៃនោះ
     */
    public static String generateDateSequentialCode(String prefix, LocalDate date, long todayCount) {
        return "%s-%s-%04d".formatted(prefix, DATE_CODE_FORMAT.format(date), todayCount + 1);
    }

    /**
     * បង្កើត slug ដែលមិនស្ទួន ដោយបន្ថែមលេខរហូតដល់ទំនេរ។
     *
     * <p>ឧទាហរណ៍៖ {@code "Angkor Wat Tour"} → {@code "angkor-wat-tour"} ហើយបើមានស្រាប់
     * → {@code "angkor-wat-tour-1"}។
     *
     * <p><b>ចំណាំអំពីភាសាខ្មែរ</b>៖ អក្សរខ្មែរមិនមែនជា {@code [a-z0-9]} ទេ ដូច្នេះចំណងជើងខ្មែរសុទ្ធ
     * នឹងសល់តែ {@value #FALLBACK_SLUG} បូកលេខ ({@code item-1}, {@code item-2})។ បើត្រូវការ slug
     * ខ្មែរពិត សូមប្តូរ regex ទៅរួមបញ្ចូល {@code \\p{IsKhmer}} — តែត្រូវចាំថា URL នឹងក្លាយជា
     * percent-encoded វែងណាស់។
     *
     * @param text          អត្ថបទចាប់ផ្តើម (ជាទូទៅជា {@code name} ឬ {@code title})
     * @param existsChecker ពិនិត្យថា slug នេះមានក្នុង database ហើយឬនៅ
     */
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

    /** បម្លែងអត្ថបទទៅ slug ដោយមិនពិនិត្យភាពស្ទួន។ */
    public static String toSlug(String text) {
        if (text == null || text.isBlank()) {
            return FALLBACK_SLUG;
        }

        String slug = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")            // ដកសញ្ញាបន្ថែមលើស្រៈ (é → e)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")       // អ្វីដែលមិនមែនអក្សរឬលេខ → សញ្ញាចុច
                .replaceAll("^-+|-+$", "");          // ដកសញ្ញាចុចនៅដើមនិងចុង

        return slug.isBlank() ? FALLBACK_SLUG : slug;
    }
}
