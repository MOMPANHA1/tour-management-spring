package co.panha.hibernate.tourmanagement.security;

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

/**
 * ដំណាក់កាល ៥ — សិទ្ធិចូលប្រើ API។
 *
 * <p>រចនាបថ៖ REST ឥតរក្សាស្ថានភាព (stateless) — គ្មាន session គ្មាន cookie។ រាល់សំណើត្រូវផ្ទុក
 * {@code Authorization: Bearer <JWT>} ដែល Keycloak ចេញឲ្យ។
 *
 * <p><b>គោលការណ៍រួម</b>៖ អ្វីដែលភ្ញៀវត្រូវឃើញមុន login (បញ្ជី Tour · ប្រភេទ · ទីតាំង · កាលវិភាគ)
 * គឺសាធារណៈ។ រាល់ការ<b>កែប្រែ</b>ទិន្នន័យមេជារបស់ {@code ADMIN}។
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** ផ្លូវឯកសារ API — ត្រូវបើកសេរី បើមិនដូច្នេះទេ Swagger UI ផ្ទុកមិនរួច។ */
    private static final String[] DOCS_WHITELIST = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        // ១. REST — គ្មាន session
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // ២. សិទ្ធិតាម endpoint
        //    លំដាប់សំខាន់ណាស់ — Spring យក matcher ដំបូងដែលត្រូវគ្នា។ ដូច្នេះច្បាប់សាធារណៈ
        //    (ជាក់លាក់ជាង) ត្រូវប្រកាសមុនច្បាប់ ADMIN (ទូលំទូលាយជាង)។
        http.authorizeHttpRequests(request -> request
                .requestMatchers(DOCS_WHITELIST).permitAll()

                // ចុះឈ្មោះ — មិនទាន់មានគណនីទេ ដូច្នេះត្រូវសាធារណៈ (F6 នឹងបង្កើត endpoint នេះ)
                .requestMatchers(HttpMethod.POST, "/api/v1/customers/register").permitAll()

                // ---------- សាធារណៈ៖ អានបាន មិនបាច់ login ----------
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/destinations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/tours/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/schedules/*").permitAll()

                // មគ្គុទ្ទេសក៍ម្នាក់ៗមើលបាន (ទំព័រព័ត៌មាន Tour) តែបញ្ជីទាំងមូល និង /available
                // ជារបស់ ADMIN — ជាទិន្នន័យប្រតិបត្តិការខាងក្នុង
                .requestMatchers(HttpMethod.GET, "/api/v1/guides/available").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/guides/*").permitAll()

                // ---------- Booking / Payment៖ ADMIN មុន ព្រោះជាករណីលើកលែងក្នុងផ្លូវរបស់ CUSTOMER ----------
                .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/*/confirm").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/bookings/*/refunds").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/payments/**").hasAuthority("ADMIN")

                // សង្ខេបទូទាត់ — ម្ចាស់ ឬ ADMIN (Service ពិនិត្យម្ចាស់ខ្លួនឯង)
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/*/payments")
                        .hasAnyAuthority("CUSTOMER", "ADMIN")

                // ---------- CUSTOMER ----------
                .requestMatchers("/api/v1/customers/me/**").hasAuthority("CUSTOMER")

                // GET /bookings/{code} បើកឲ្យទាំង CUSTOMER និង ADMIN — Service ពិនិត្យម្ចាស់ខ្លួនឯង
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/*").hasAnyAuthority("CUSTOMER", "ADMIN")
                .requestMatchers("/api/v1/bookings/**").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/reviews/**").hasAuthority("CUSTOMER")

                // ---------- ADMIN៖ ការកែប្រែទិន្នន័យមេទាំងអស់ ----------
                .requestMatchers(
                        "/api/v1/categories/**",
                        "/api/v1/destinations/**",
                        "/api/v1/guides/**",
                        "/api/v1/tours/**",
                        "/api/v1/schedules/**",
                        "/api/v1/customers/**"
                ).hasAuthority("ADMIN")

                .anyRequest().authenticated());

        // ៣. យន្តការផ្ទៀងផ្ទាត់ — JWT ពី Keycloak
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults()));

        // ៤. បិទ CSRF — គ្មាន cookie ដូច្នេះគ្មានហានិភ័យ CSRF
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * បម្លែង role របស់ Keycloak ទៅជា authority របស់ Spring។
     *
     * <p>Keycloak ដាក់ role ក្នុង claim ដែលមានរចនាសម្ព័ន្ធ
     * {@code {"realm_access": {"roles": ["ADMIN", "USER"]}}} — Spring មិនស្គាល់ទម្រង់នេះទេ
     * ដូច្នេះត្រូវបម្លែងដោយដៃ។
     *
     * <p><b>គ្មាន prefix {@code ROLE_}</b> — ដូច្នេះត្រូវប្រើ {@code hasAuthority("ADMIN")}។
     * បើប្រើ {@code hasRole("ADMIN")} Spring នឹងបន្ថែម {@code ROLE_} ដោយស្វ័យប្រវត្តិ
     * ហើយ<b>មិនដែលផ្គូផ្គង</b>ជាដាច់ខាត។
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        Converter<Jwt, Collection<GrantedAuthority>> converter = jwt -> {
            Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess == null) {
                return Set.of();
            }

            Collection<String> roles = realmAccess.get("roles");

            if (roles == null) {
                return Set.of();
            }

            return roles.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                    .collect(Collectors.toSet());
        };

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtAuthenticationConverter;
    }
}
