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

// Global handler mapping every exception to a uniform ApiErrorResponse; API messages are English.
@Slf4j
@RestControllerAdvice
public class GlobalAppException {

    // Bean validation errors: collect all invalid fields so the frontend can show them at once.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // Keep the first message per field.
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Invalid request payload", request, fieldErrors);
    }

    // Unreadable body (bad JSON, unknown enum, wrong type) is a client error (400), not 500.
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

    // Unconvertible query param or path variable, e.g. ?page=abc.
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

    // Deliberate service errors carry their status code and English message.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex,
                                                                 HttpServletRequest request) {
        HttpStatusCode status = ex.getStatusCode();
        return build(status, reasonPhrase(status), ex.getReason(), request, null);
    }

    // DB constraint violation; reaching here means a race condition or a missing service check, so log it.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        log.warn("DataIntegrityViolation at {} — {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        return build(HttpStatus.CONFLICT, "Conflict",
                "Request conflicts with existing data", request, null);
    }

    // Concurrent edit of the same row; applies to Booking (@Version).
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                                 HttpServletRequest request) {
        log.warn("OptimisticLockingFailure at {}", request.getRequestURI());

        return build(HttpStatus.CONFLICT, "Conflict",
                "The record was modified by someone else — please retry", request, null);
    }

    // TODO: phase 5, add a handler for AccessDeniedException -> 403 (needs spring-boot-starter-security).

    // Unexpected errors; ErrorResponse is checked first so standard Spring exceptions keep their own status.
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

    // ---------- internal helpers ----------

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

    // Finds an InvalidFormatException in the cause chain (records wrap it in ValueInstantiationException).
    private InvalidFormatException findInvalidFormat(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof InvalidFormatException found) {
                return found;
            }
            if (current.getCause() == current) {
                break;                      // guard against endless loop
            }
        }
        return null;
    }

    // JSON path of the failing field; uses Jackson 3 (tools.jackson.*), which Spring Boot 4 requires.
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
