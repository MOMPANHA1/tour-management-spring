package co.panha.hibernate.tourmanagement.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

/**
 * ជំនួយអានអត្តសញ្ញាណអ្នកប្រើពី JWT ដែល Keycloak ចេញឲ្យ។
 *
 * <p>ប្រើក្នុង Service មិនមែនក្នុង Controller ទេ — ដើម្បីកុំឲ្យវិន័យអាជីវកម្ម (ម្ចាស់ធនធានជានរណា)
 * ខ្ចាត់ខ្ចាយចេញពី Service layer។
 *
 * <p><b>ចំណាំអំពី authority</b>៖ {@code SecurityConfig.jwtAuthenticationConverter()} បម្លែង
 * {@code realm_access.roles} ទៅជា authority <b>ដោយគ្មាន prefix</b> {@code ROLE_}។
 * ដូច្នេះត្រូវប្រើ {@code hasAuthority("ADMIN")} — មិនមែន {@code hasRole("ADMIN")} ទេ។
 */
public final class AuthUtils {

    private AuthUtils() {
    }

    /**
     * Keycloak User ID ({@code sub} claim) — តម្លៃដែល<b>មិនប្តូរជារៀងរហូត</b>។
     *
     * <p>ប្រើវាជាស្ពានទៅតារាង {@code customers} ({@code Customer.keycloakId}) មិនមែន
     * {@code username} ទេ ព្រោះ username អាចប្តូរបានក្នុង Keycloak។
     */
    public static String currentKeycloakId() {
        return requireJwtToken().getToken().getSubject();
    }

    /** {@code preferred_username} claim — សម្រាប់បង្ហាញ និង log ប៉ុណ្ណោះ មិនមែនជាកូនសោទេ។ */
    public static String currentUsername() {
        return requireJwtToken().getToken().getClaimAsString("preferred_username");
    }

    /** {@code email} claim — អាច {@code null} បើ realm មិនតម្រូវឲ្យបំពេញ email។ */
    public static String currentEmail() {
        return requireJwtToken().getToken().getClaimAsString("email");
    }

    public static boolean isAdmin() {
        return hasAuthority(KeycloakRoleEnum.ADMIN.name());
    }

    public static boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    /**
     * អនុញ្ញាតតែម្ចាស់ធនធាន ឬ ADMIN — បោះ <b>403</b> បើមិនមែន។
     *
     * <p>បោះ 403 មិនមែន 404 ទេ ព្រោះអ្នកប្រើបាន authenticate រួចហើយ ហើយធនធាន<b>មាន</b>ពិត —
     * គ្រាន់តែគាត់គ្មានសិទ្ធិប៉ុណ្ណោះ។
     */
    public static void requireOwnerOrAdmin(String ownerKeycloakId) {
        if (isAdmin()) {
            return;
        }

        if (!currentKeycloakId().equals(ownerKeycloakId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to access this resource");
        }
    }

    /**
     * ទាញ token ឬបោះ <b>401</b>។
     *
     * <p>ពិនិត្យ {@code null} និង {@code AnonymousAuthenticationToken} មុន cast — បើមិនដូច្នេះទេ
     * endpoint ដែល {@code permitAll} នឹងបោះ {@code ClassCastException} ក្លាយជា <b>500</b>។
     */
    private static JwtAuthenticationToken requireJwtToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return jwtAuthenticationToken;
    }
}
