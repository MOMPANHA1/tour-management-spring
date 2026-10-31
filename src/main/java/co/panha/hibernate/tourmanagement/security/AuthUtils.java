package co.panha.hibernate.tourmanagement.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

// Reads the user identity from the Keycloak JWT; authorities have no ROLE_ prefix, use hasAuthority.
public final class AuthUtils {

    private AuthUtils() {
    }

    // Keycloak user ID (sub claim); immutable, so it links to Customer.keycloakId instead of username.
    public static String currentKeycloakId() {
        return requireJwtToken().getToken().getSubject();
    }

    // preferred_username claim; for display and logs only, not a key.
    public static String currentUsername() {
        return requireJwtToken().getToken().getClaimAsString("preferred_username");
    }

    // email claim; may be null if the realm does not require it.
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

    // Allows only the owner or ADMIN; throws 403 (not 404) since the resource exists but access is denied.
    public static void requireOwnerOrAdmin(String ownerKeycloakId) {
        if (isAdmin()) {
            return;
        }

        if (!currentKeycloakId().equals(ownerKeycloakId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to access this resource");
        }
    }

    // Returns the token or throws 401; checks null/anonymous first to avoid a ClassCastException (500).
    private static JwtAuthenticationToken requireJwtToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return jwtAuthenticationToken;
    }
}
