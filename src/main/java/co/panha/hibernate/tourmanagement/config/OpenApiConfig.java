package co.panha.hibernate.tourmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ឯកសារ API (Swagger UI) — {@code http://localhost:9090/swagger-ui.html}។
 *
 * <p>តួនាទីសំខាន់បំផុតគឺបន្ថែមប៊ូតុង <b>Authorize</b>។ បើគ្មានវាទេ រាល់ endpoint ដែលត្រូវការសិទ្ធិ
 * នឹងត្រឡប់ <b>401</b> ពេលចុច "Try it out" ព្រោះ Swagger មិនផ្ញើ header
 * {@code Authorization} ទៅជាមួយឡើយ។
 *
 * <p>ផ្តល់ជម្រើស <b>ពីរ</b> ដើម្បី authorize (ជ្រើសមួយណាក៏បាន)៖
 *
 * <ol>
 *   <li><b>{@code bearerAuth}</b> — paste token ដែលយកមកពី curl ដោយផ្ទាល់។
 *       ដំណើរការជានិច្ច គ្មានការរៀបចំបន្ថែម។</li>
 *   <li><b>{@code keycloak}</b> — វាយ username/password ក្នុង Swagger ហើយវាទៅយក token ឲ្យខ្លួនឯង។
 *       ស្រួលជាង តែត្រូវរៀបចំ <b>Web origins</b> ក្នុង Keycloak សិន (មើលខាងក្រោម)។</li>
 * </ol>
 *
 * <p><b>ការរៀបចំសម្រាប់ជម្រើសទី ២</b> — Swagger UI (port 9090) ហៅ Keycloak (port 1001)
 * ឆ្លងកាត់ browser ដូច្នេះត្រូវការ CORS៖
 * <pre>
 * Clients → tour-backend → Settings → Web origins → បន្ថែម  http://localhost:9090
 * </pre>
 * បើខ្វះជំហាននេះ browser នឹងបិទសំណើដោយស្ងាត់ ហើយ Swagger បង្ហាញត្រឹម "Auth ErrorTypeError:
 * Failed to fetch" ដែលមិនប្រាប់មូលហេតុពិតទេ។
 */
@Configuration
public class OpenApiConfig {

    /** យក issuer-uri ដែលមានស្រាប់ធ្វើជាប្រភពតែមួយ — កុំសរសេរ URL ម្តងទៀត។ */
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String OAUTH2_SCHEME = "keycloak";

    @Bean
    public OpenAPI tourManagementOpenApi() {

        return new OpenAPI()
                .info(new Info()
                        .title("Tour Management API")
                        .version("v1")
                        .description("""
                                API គ្រប់គ្រងកញ្ចប់ដំណើរកម្សាន្ត។

                                ការផ្ទៀងផ្ទាត់អត្តសញ្ញាណធ្វើដោយ Keycloak — ចុច **Authorize** មុនសាកល្បង \
                                endpoint ដែលត្រូវការសិទ្ធិ។ Endpoint សាធារណៈ (GET លើ categories, \
                                destinations, tours, schedules) មិនត្រូវការ token ទេ។"""))

                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, bearerScheme())
                        .addSecuritySchemes(OAUTH2_SCHEME, keycloakScheme()))

                // SecurityRequirement ដាច់ដោយឡែកពីគ្នា មានន័យថា "មួយណាក៏បាន" (OR)។
                // បើដាក់ទាំងពីរក្នុង object តែមួយ វានឹងក្លាយជា "ត្រូវមានទាំងពីរ" (AND) វិញ។
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2_SCHEME));
    }

    /** ជម្រើសទី ១ — paste token ដោយផ្ទាល់។ */
    private SecurityScheme bearerScheme() {
        return new SecurityScheme()
                .name(BEARER_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste តែ token ទទេៗ — កុំដាក់ពាក្យ \"Bearer \" នៅខាងមុខ។");
    }

    /**
     * ជម្រើសទី ២ — Swagger ទៅយក token ឲ្យខ្លួនឯង។
     *
     * <p>ប្រើ {@code password} flow ដូច curl ដែរ។ Swagger UI នឹងសួររក {@code client_id}
     * និង {@code client_secret} ក្នុងប្រអប់ Authorize ដូច្នេះ <b>មិនចាំបាច់</b>ដាក់ secret
     * ក្នុង {@code application.properties} ទេ។
     */
    private SecurityScheme keycloakScheme() {
        OAuthFlow passwordFlow = new OAuthFlow()
                .tokenUrl(issuerUri + "/protocol/openid-connect/token");

        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("វាយ username/password (និង client_id = tour-backend + secret)។")
                .flows(new OAuthFlows().password(passwordFlow));
    }
}
