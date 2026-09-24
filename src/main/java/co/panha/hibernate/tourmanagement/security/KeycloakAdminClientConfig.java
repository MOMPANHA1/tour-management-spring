package co.panha.hibernate.tourmanagement.security;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Client សម្រាប់ហៅ Keycloak Admin REST API — ប្រើពេលបង្កើត user ក្នុង F6 Customer។
 *
 * <p>ប្រើ {@code CLIENT_CREDENTIALS} មានន័យថា backend ចូលដោយខ្លួនឯង (service account)
 * មិនមែនតំណាងឲ្យ user ណាម្នាក់ទេ។
 *
 * <p><b>ត្រូវរៀបចំក្នុង Keycloak មុន</b> — បើខ្វះ ការបង្កើត user នឹងត្រឡប់ <b>403</b>៖
 * <ol>
 *   <li>Client {@code tour-backend} → {@code Client authentication: On}</li>
 *   <li>Client {@code tour-backend} → {@code Service accounts roles: On}</li>
 *   <li>Service account roles → assign ពី client {@code realm-management}៖
 *       {@code manage-users}, {@code view-users}, {@code query-groups}, {@code view-realm}</li>
 * </ol>
 *
 * <p>{@code KeycloakBuilder.build()} មិនតភ្ជាប់ភ្លាមទេ — ដូច្នេះ application ចាប់ផ្តើមបាន
 * ទោះបី Keycloak មិនទាន់រត់ក៏ដោយ។
 */
@Configuration
public class KeycloakAdminClientConfig {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}
