package co.panha.hibernate.tourmanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ការគ្រប់គ្រងកំហុសសកល — បម្លែងរាល់ exception ទៅជា {@link ApiErrorResponse} ដែលមានរូបរាងតែមួយ។
 *
 * <p>គោលការណ៍៖ អ្នកប្រើមិនគួរឃើញ stack trace ឬសារឆៅពី framework ទេ — គ្រប់សារ API ត្រូវជាភាសាអង់គ្លេស
 * (ឯ javadoc និងមតិយោបល់ក្នុងកូដនៅជាភាសាខ្មែរ)។
 */
@Slf4j
@RestControllerAdvice
public class GlobalAppException {

    /**
     * កំហុស validation ពី {@code @Valid} — ប្រមូលរាល់ field ដែលខុសជាមួយគ្នា
     * ដើម្បីឲ្យ frontend បង្ហាញក្រោម input នីមួយៗបានតែម្តង។
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // រក្សាសារដំបូងនៃ field នីមួយៗ — កុំឲ្យសារទី ២ ជាន់សារទី ១
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Invalid request payload", request, fieldErrors);
    }

    /**
     * Body ដែលអាន​មិន​បាន — JSON ខូចទម្រង់, enum មិនស្គាល់, ឬប្រភេទតម្លៃខុស។
     *
     * <p>បើគ្មាន handler នេះ ករណីសាមញ្ញដូចជា {@code "gender":"UNKNOWN"} នឹងក្លាយជា <b>500</b>
     * ទាំងដែលវាជាកំហុសរបស់ client។ Spring មិនបម្លែងវាដោយខ្លួនឯងទេ ព្រោះ
     * {@code HttpMessageNotReadableException} មិនមែនជា {@code ErrorResponse}។
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        Map<String, String> fieldErrors = null;
        InvalidFormatException cause = findInvalidFormat(ex);

        if (cause != null) {
            String field = fieldPath(cause);
            Class<?> targetType = cause.getTargetType();

            String allowed = (targetType != null && targetType.isEnum())
                    ? "allowed values: " + Arrays.toString(targetType.getEnumConstants())
                    : "expected type " + (targetType != null ? targetType.getSimpleName() : "unknown");

            fieldErrors = Map.of(field, "Invalid value — " + allowed);
        }

        return build(HttpStatus.BAD_REQUEST, "Bad Request",
                "Malformed request body", request, fieldErrors);
    }

    /** Query param ឬ path variable ដែលបម្លែងមិនបាន — ឧ. {@code ?page=abc} ឬ {@code ?status=BOGUS}។ */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        Class<?> requiredType = ex.getRequiredType();

        String allowed = (requiredType != null && requiredType.isEnum())
                ? "allowed values: " + Arrays.toString(requiredType.getEnumConstants())
                : "expected type " + (requiredType != null ? requiredType.getSimpleName() : "unknown");

        return build(HttpStatus.BAD_REQUEST, "Bad Request",
                "Invalid parameter value", request,
                Map.of(ex.getName(), "Invalid value — " + allowed));
    }

    /** កំហុសដែល Service បោះដោយចេតនា — ផ្ទុកលេខកូដ និងសារជាភាសាអង់គ្លេសរួចស្រេច។ */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex,
                                                                 HttpServletRequest request) {
        HttpStatusCode status = ex.getStatusCode();
        return build(status, reasonPhrase(status), ex.getReason(), request, null);
    }

    /**
     * ការប៉ះទង្គិច constraint នៅកម្រិត database (unique, foreign key)។
     *
     * <p>ជាធម្មតា Service គួរចាប់បាន<b>មុន</b>ដល់ត្រង់នេះ (ឧ. {@code existsByName} មុន save)។
     * ការមកដល់ទីនេះមានន័យថាមាន race condition ឬខ្វះការពិនិត្យ — ដូច្នេះកត់ត្រាទុកដើម្បីពិនិត្យក្រោយ។
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        log.warn("DataIntegrityViolation at {} — {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        return build(HttpStatus.CONFLICT, "Conflict",
                "Request conflicts with existing data", request, null);
    }

    /** មនុស្ស ២ នាក់កែទិន្នន័យដដែលព្រមគ្នា — កើតលើ {@code Booking} ដែលមាន {@code @Version}។ */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                                 HttpServletRequest request) {
        log.warn("OptimisticLockingFailure at {}", request.getRequestURI());

        return build(HttpStatus.CONFLICT, "Conflict",
                "The record was modified by someone else — please retry", request, null);
    }

    // TODO ដំណាក់កាល ៥៖ បន្ថែម handler សម្រាប់ AccessDeniedException -> 403
    //   (ត្រូវការ spring-boot-starter-security ដែលមិនទាន់ដាក់ក្នុង build.gradle)

    /**
     * កំហុសដែលមិនបានរំពឹងទុក។
     *
     * <p>ពិនិត្យ {@link ErrorResponse} ជាមុនសិន ព្រោះ Spring បោះ exception ស្តង់ដារជាច្រើន
     * ({@code NoResourceFoundException} 404, {@code HttpRequestMethodNotSupportedException} 405,
     * {@code HttpMessageNotReadableException} 400) ដែលមានលេខកូដត្រឹមត្រូវរួចហើយ — បើមិនពិនិត្យ
     * ពួកវានឹងក្លាយជា 500 ទាំងអស់ ហើយ log នឹងពេញដោយកំហុសក្លែងក្លាយ។
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            return build(status, reasonPhrase(status), ex.getMessage(), request, null);
        }

        log.error("Unexpected error at {} {}", request.getMethod(), request.getRequestURI(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An internal error occurred — please contact the administrator", request, null);
    }

    // ---------- ជំនួយខាងក្នុង ----------

    private ResponseEntity<ApiErrorResponse> build(HttpStatusCode status, String error, String message,
                                                   HttpServletRequest request, Map<String, String> fieldErrors) {
        ApiErrorResponse body = ApiErrorResponse.of(
                status.value(),
                error,
                (message == null || message.isBlank()) ? "An error occurred" : message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }

    /**
     * ស្វែងរក {@link InvalidFormatException} តាមខ្សែសង្វាក់ cause។
     *
     * <p>ការពិនិត្យត្រឹម {@code getCause()} មិនគ្រប់គ្រាន់ទេ — ពេលបម្លែងចូល {@code record}
     * Jackson រុំកំហុសក្នុង {@code ValueInstantiationException} មួយស្រទាប់ទៀត។
     */
    private InvalidFormatException findInvalidFormat(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof InvalidFormatException found) {
                return found;
            }
            if (current.getCause() == current) {
                break;                      // ការពារ loop មិនចេះចប់
            }
        }
        return null;
    }

    /**
     * ផ្លូវ field ក្នុង JSON ដែលបង្កកំហុស — ឧ. {@code "gender"} ឬ {@code "passengers[0].gender"}។
     *
     * <p>Spring Boot 4 ប្រើ <b>Jackson 3</b> ({@code tools.jackson.*}) មិនមែន Jackson 2
     * ({@code com.fasterxml.jackson.*}) ទេ — ទោះបី Jackson 2 នៅលើ classpath ក៏ដោយ។
     * ការប្រើថ្នាក់ខុស package នឹង compile ជាប់ តែ {@code instanceof} នឹងមិនដែលត្រូវ។
     */
    private String fieldPath(JacksonException ex) {
        String path = ex.getPath().stream()
                .map(JacksonException.Reference::getPropertyName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));

        return path.isBlank() ? "body" : path;
    }

    private String reasonPhrase(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return (resolved != null) ? resolved.getReasonPhrase() : "Error";
    }
}
