package co.panha.hibernate.tourmanagement.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Phase 5: stateless REST security; pre-login browsing is public, data changes are ADMIN only.
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // API docs paths must be open or Swagger UI cannot load.
    private static final String[] DOCS_WHITELIST = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    // Must match keycloak.client-id; used to read client roles from resource_access.
    @Value("${keycloak.client-id}")
    private String clientId;

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        // 1. REST: no session
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 2. Per-endpoint rules; order matters (first match wins), so specific public rules go before broad ADMIN rules.
        http.authorizeHttpRequests(request -> request
                .requestMatchers(DOCS_WHITELIST).permitAll()

                // Registration is public since no account exists yet.
                .requestMatchers(HttpMethod.POST, "/api/v1/customers/register").permitAll()

                // ---------- Public: readable without login ----------
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/destinations/**").permitAll()
                // Also covers /tours/{id}/reviews and /reviews/summary.
                .requestMatchers(HttpMethod.GET, "/api/v1/tours/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/schedules/*").permitAll()

                // A single guide is public (tour page); the full list and /available are ADMIN-only operational data.
                .requestMatchers(HttpMethod.GET, "/api/v1/guides/available").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/guides/*").permitAll()

                // ---------- Booking / Payment: ADMIN first, as exceptions within CUSTOMER paths ----------
                .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/*/confirm").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/bookings/*/refunds").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/payments/**").hasAuthority("ADMIN")

                // ---------- Review: ADMIN reply and hide ----------
                .requestMatchers(HttpMethod.POST, "/api/v1/reviews/*/reply").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/reviews/*/hide").hasAuthority("ADMIN")

                // Payment summary: owner or ADMIN (service checks ownership).
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/*/payments")
                        .hasAnyAuthority("CUSTOMER", "ADMIN")

                // ---------- CUSTOMER ----------
                .requestMatchers("/api/v1/customers/me/**").hasAuthority("CUSTOMER")

                // GET /bookings/{code} is open to CUSTOMER and ADMIN; the service checks ownership.
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/*").hasAnyAuthority("CUSTOMER", "ADMIN")
                .requestMatchers("/api/v1/bookings/**").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/reviews/**").hasAuthority("CUSTOMER")

                // ---------- ADMIN: all master data changes ----------
                .requestMatchers(
                        "/api/v1/categories/**",
                        "/api/v1/destinations/**",
                        "/api/v1/guides/**",
                        "/api/v1/tours/**",
                        "/api/v1/schedules/**",
                        "/api/v1/customers/**"
                ).hasAuthority("ADMIN")

                .anyRequest().authenticated());

        // 3. Authentication: JWT from Keycloak
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults()));

        // 4. CSRF disabled: no cookies, so no CSRF risk
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // Maps Keycloak realm and client roles to uppercase authorities without ROLE_ (use hasAuthority).
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        Converter<Jwt, Collection<GrantedAuthority>> converter = jwt -> {
            Set<GrantedAuthority> authorities = new java.util.HashSet<>();

            Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") != null) {
                realmAccess.get("roles").forEach(role ->
                        authorities.add(new SimpleGrantedAuthority(role.toUpperCase())));
            }

            Map<String, Map<String, Collection<String>>> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null && resourceAccess.get(clientId) != null) {
                Collection<String> clientRoles = resourceAccess.get(clientId).get("roles");
                if (clientRoles != null) {
                    clientRoles.forEach(role ->
                            authorities.add(new SimpleGrantedAuthority(role.toUpperCase())));
                }
            }

            return authorities;
        };

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtAuthenticationConverter;
    }
}
