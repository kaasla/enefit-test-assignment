package com.kaarelkaasla.enefitresourceservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.auditing.DateTimeProvider;

class JpaAuditingConfigTest {

  private final JpaAuditingConfig jpaAuditingConfig = new JpaAuditingConfig();

  @Test
  void auditingDateTimeProvider_ShouldReturnCurrentTimeInApplicationZone() {
    ZoneId testZoneId = ZoneId.of("America/New_York");

    DateTimeProvider result = jpaAuditingConfig.auditingDateTimeProvider(testZoneId);

    assertThat(result).isNotNull();

    Optional<?> dateTimeOpt = result.getNow();
    assertThat(dateTimeOpt).isPresent();

    OffsetDateTime dateTime = (OffsetDateTime) dateTimeOpt.get();
    OffsetDateTime now = OffsetDateTime.now(testZoneId);

    long timeDiffSeconds = Math.abs(java.time.Duration.between(dateTime, now).getSeconds());
    assertThat(timeDiffSeconds).isLessThanOrEqualTo(2L);

    assertThat(dateTime.getOffset())
        .isEqualTo(testZoneId.getRules().getOffset(dateTime.toInstant()));
  }

  @Test
  void auditingDateTimeProvider_WithUtcZone_ShouldReturnUtcTime() {
    ZoneId utcZoneId = ZoneId.of("UTC");

    DateTimeProvider result = jpaAuditingConfig.auditingDateTimeProvider(utcZoneId);

    Optional<?> dateTimeOpt = result.getNow();
    assertThat(dateTimeOpt).isPresent();
    OffsetDateTime dateTime = (OffsetDateTime) dateTimeOpt.get();
    assertThat(dateTime.getOffset().getTotalSeconds()).isZero();
  }
}
