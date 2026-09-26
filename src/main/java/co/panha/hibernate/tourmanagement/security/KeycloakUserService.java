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

/**
 * កិច្ចការទាំងឡាយដែលប៉ះ Keycloak ដោយផ្ទាល់ — ញែកចេញពី {@code CustomerServiceImpl}
 * ដើម្បីឲ្យ Service នៅតែនិយាយរឿងអាជីវកម្ម មិនមែនរឿង protocol។
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * បង្កើត user ក្នុង Keycloak ហើយប្រគល់ {@code keycloakId} ({@code sub}) មកវិញ។
     *
     * <p>កំណត់ {@code firstName} ពី {@code fullName} ទាំងមូល ហើយទុក {@code lastName} ទទេ —
     * ព្រោះ domain នេះប្រើឈ្មោះពេញតែមួយ។ ដំណើរការនេះត្រូវការឲ្យ realm បិទ
     * <b>Required</b> លើ {@code lastName} ក្នុង <i>Realm settings → User profile</i>។
     *
     * @throws ResponseStatusException 409 បើ username ឬ email ស្ទួនក្នុង Keycloak
     */
    public String createUser(String username, String email, String fullName, String password) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(fullName);
        user.setEnabled(true);

        // បើទុក false នោះ Keycloak បន្ថែម required action VERIFY_EMAIL ហើយ login បរាជ័យ
        // ដោយសារ "Account is not fully set up"។
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
                // ស្ទើរតែជានិច្ចមកពី service account ខ្វះ role `manage-users` ក្នុង realm-management
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "The backend is not allowed to create users in the identity provider");
            }

            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Identity provider rejected the registration (status " + status + ")");
        }
    }

    /**
     * លុប user ចោល — ប្រើជា <b>compensation</b> ពេលការរក្សាទុកក្នុង database បរាជ័យ
     * បន្ទាប់ពី Keycloak បង្កើត user រួច។
     *
     * <p>មិនបោះ exception ទេ ព្រោះវាត្រូវហៅក្នុង {@code catch} — បើវាបោះ វានឹងបិទបាំង
     * កំហុសដើមដែលសំខាន់ជាង។
     */
    public void deleteUserQuietly(String keycloakId) {
        try (Response response = users().delete(keycloakId)) {
            log.warn("Compensating: deleted Keycloak user id={} status={}", keycloakId, response.getStatus());
        } catch (RuntimeException ex) {
            log.error("Compensation FAILED — orphan Keycloak user id={} must be removed manually",
                    keycloakId, ex);
        }
    }

    /** បិទ/បើក user — ប្រើពេល ADMIN ផ្អាក ឬដោះការផ្អាកគណនី។ */
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

    /**
     * ទាញ id ពី header {@code Location} របស់ response។
     *
     * <p>ល្អជាងការស្វែងរកតាម username ({@code users().search(username)}) ដែល ecommerce ប្រើ —
     * ការស្វែងរកនោះអាចប្រគល់ user ខុស ព្រោះវាជា <i>partial match</i> ហើយត្រូវហៅ Keycloak
     * បន្ថែមមួយជុំទៀត។
     */
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
