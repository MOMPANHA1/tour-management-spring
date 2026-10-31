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

// Swagger UI config; adds the Authorize button (bearerAuth token or Keycloak password flow).
@Configuration
public class OpenApiConfig {

    // Reuse the existing issuer-uri as the single source of the Keycloak URL.
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

                // Separate SecurityRequirement entries mean OR; putting both in one object would mean AND.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2_SCHEME));
    }

    // Option 1: paste a bearer token directly.
    private SecurityScheme bearerScheme() {
        return new SecurityScheme()
                .name(BEARER_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste តែ token ទទេៗ — កុំដាក់ពាក្យ \"Bearer \" នៅខាងមុខ។");
    }

    // Option 2: Swagger fetches the token itself (password flow); needs Keycloak Web origins set for CORS.
    private SecurityScheme keycloakScheme() {
        OAuthFlow passwordFlow = new OAuthFlow()
                .tokenUrl(issuerUri + "/protocol/openid-connect/token");

        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("វាយ username/password (និង client_id = tour-backend + secret)។")
                .flows(new OAuthFlows().password(passwordFlow));
    }
}
