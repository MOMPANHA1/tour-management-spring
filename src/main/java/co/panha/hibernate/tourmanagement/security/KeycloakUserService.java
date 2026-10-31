package co.panha.hibernate.tourmanagement.security;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Keycloak operations, separated from CustomerServiceImpl to keep it business-focused.
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    // Creates a Keycloak user and returns its id; realm must not require lastName.
    public String createUser(String username, String email, String fullName, String password) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(fullName);
        user.setEnabled(true);

        // Keep false, otherwise Keycloak adds VERIFY_EMAIL and login fails with 'Account is not fully set up'.
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        try (Response response = users().create(user)) {
            int status = response.getStatus();

            if (status == HttpStatus.CREATED.value()) {
                String keycloakId = extractCreatedId(response);
                assignCustomerRoles(keycloakId);
                log.info("Keycloak user created: username={} id={}", username, keycloakId);
                return keycloakId;
            }

            log.warn("Keycloak user creation failed: username={} status={}", username, status);

            if (status == HttpStatus.CONFLICT.value()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Username or email already exists in the identity provider");
            }

            if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
                // Almost always the service account lacks the manage-users role in realm-management.
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "The backend is not allowed to create users in the identity provider");
            }

            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Identity provider rejected the registration (status " + status + ")");
        }
    }

    // Compensation when the DB save fails after the user was created; must not throw inside a catch block.
    public void deleteUserQuietly(String keycloakId) {
        try (Response response = users().delete(keycloakId)) {
            log.warn("Compensating: deleted Keycloak user id={} status={}", keycloakId, response.getStatus());
        } catch (RuntimeException ex) {
            log.error("Compensation FAILED — orphan Keycloak user id={} must be removed manually",
                    keycloakId, ex);
        }
    }

    // Enables or disables a user (ADMIN suspend / unsuspend).
    public void setEnabled(String keycloakId, boolean enabled) {
        UserResource userResource = users().get(keycloakId);

        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(enabled);
        userResource.update(user);

        log.info("Keycloak user id={} enabled={}", keycloakId, enabled);
    }

    private void assignCustomerRoles(String keycloakId) {
        List<RoleRepresentation> roles = List.of(
                keycloak.realm(realm).roles().get(KeycloakRoleEnum.USER.name()).toRepresentation(),
                keycloak.realm(realm).roles().get(KeycloakRoleEnum.CUSTOMER.name()).toRepresentation()
        );

        users().get(keycloakId).roles().realmLevel().add(roles);
    }

    // Extracts the id from the Location header; more reliable than a partial-match username search.
    private String extractCreatedId(Response response) {
        String location = response.getLocation() != null ? response.getLocation().getPath() : null;

        if (location == null || !location.contains("/")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Identity provider did not return the created user id");
        }

        return location.substring(location.lastIndexOf('/') + 1);
    }

    private UsersResource users() {
        return keycloak.realm(realm).users();
    }
}
