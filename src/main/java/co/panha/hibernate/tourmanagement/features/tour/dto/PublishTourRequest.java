package co.panha.hibernate.tourmanagement.features.tour.dto;

import jakarta.validation.constraints.NotNull;

/**
 * បិទ/បើកលក់ Tour — UC4.5។
 *
 * <p>ប្រើ {@code Boolean} មិនមែន {@code boolean} ដើម្បីឲ្យ {@code @NotNull} ចាប់បាន
 * ពេល client ភ្លេចផ្ញើ field នេះ — បើមិនដូច្នេះវានឹងក្លាយជា {@code false} ដោយស្ងាត់ៗ
 * ហើយបិទលក់ Tour ដោយអចេតនា។
 */
public record PublishTourRequest(

        @NotNull(message = "Published flag is required")
        Boolean published
) {
}
