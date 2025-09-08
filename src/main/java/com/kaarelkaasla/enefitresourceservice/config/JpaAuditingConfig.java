package com.kaarelkaasla.enefitresourceservice.config;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing using the configured application timezone.
 * Supplies a DateTimeProvider used for created/updated audit fields.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

  @Bean
  public DateTimeProvider auditingDateTimeProvider(ZoneId applicationZoneId) {
    return () -> Optional.of(OffsetDateTime.now(applicationZoneId));
  }
}
