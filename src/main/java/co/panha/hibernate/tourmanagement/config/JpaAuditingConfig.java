package co.panha.hibernate.tourmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * បើក Spring Data JPA Auditing ដើម្បីឲ្យ {@code @CreatedDate} និង {@code @LastModifiedDate}
 * នៅក្នុង {@code base/BaseEntity} ទទួលតម្លៃដោយស្វ័យប្រវត្តិ។
 *
 * <p><b>សំខាន់</b>៖ បើថ្នាក់នេះបាត់ {@code createdAt} និង {@code updatedAt} នឹង {@code null} ជានិច្ច
 * ដោយ<b>គ្មានកំហុសបង្ហាញ</b> — ជាកំហុសដែលរកឃើញយឺតបំផុតមួយ។
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
