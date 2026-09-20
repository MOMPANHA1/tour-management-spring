package co.panha.hibernate.tourmanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * រូបរាងឆ្លើយតបរួមសម្រាប់គ្រប់កំហុស។
 *
 * <p>{@code fieldErrors} មានតម្លៃតែពេលកំហុស validation ប៉ុណ្ណោះ — ករណីផ្សេងវាត្រូវលាក់ចេញពី JSON
 * ដោយ {@code @JsonInclude(NON_NULL)}។
 *
 * @param timestamp   ពេលកើតកំហុស
 * @param status      លេខកូដ HTTP
 * @param error       ឈ្មោះកំហុសខ្លី (ឧ. {@code "Conflict"})
 * @param message     សារជាភាសាខ្មែរសម្រាប់អ្នកប្រើ
 * @param path        URI ដែលស្នើសុំ
 * @param fieldErrors ផែនទី field → សារ (validation តែប៉ុណ្ណោះ)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    /** កំហុសទូទៅ — គ្មាន {@code fieldErrors}។ */
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    /** កំហុស validation — មាន {@code fieldErrors}។ */
    public static ApiErrorResponse of(int status, String error, String message, String path,
                                      Map<String, String> fieldErrors) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }
}
