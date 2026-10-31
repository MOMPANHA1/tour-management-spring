package co.panha.hibernate.tourmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Enables JPA auditing; without it createdAt/updatedAt stay null with no error.
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
