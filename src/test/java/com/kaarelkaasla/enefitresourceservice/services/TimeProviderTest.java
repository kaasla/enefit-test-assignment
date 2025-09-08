package com.kaarelkaasla.enefitresourceservice.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class TimeProviderTest {

  @Test
  void now_ShouldReturnCurrentTimeInApplicationZone() {
    ZoneId testZoneId = ZoneId.of("America/New_York");
    TimeProvider timeProvider = new TimeProvider(testZoneId);

    OffsetDateTime result = timeProvider.now();

    assertThat(result.getOffset()).isEqualTo(testZoneId.getRules().getOffset(result.toInstant()));
  }

  @Test
  void toApplicationOffset_WithNullValue_ShouldReturnNull() {
    ZoneId testZoneId = ZoneId.of("UTC");
    TimeProvider timeProvider = new TimeProvider(testZoneId);

    OffsetDateTime result = timeProvider.toApplicationOffset(null);

    assertThat(result).isNull();
  }

  @Test
  void toApplicationOffset_WithOffsetDateTime_ShouldConvertToApplicationZone() {
    ZoneId applicationZoneId = ZoneId.of("America/New_York");
    TimeProvider timeProvider = new TimeProvider(applicationZoneId);

    OffsetDateTime utcTime = OffsetDateTime.parse("2023-06-15T12:00:00Z");

    OffsetDateTime result = timeProvider.toApplicationOffset(utcTime);

    assertThat(result.toInstant()).isEqualTo(utcTime.toInstant());
    assertThat(result.getOffset())
        .isEqualTo(applicationZoneId.getRules().getOffset(utcTime.toInstant()));
  }

  @Test
  void toApplicationOffset_WithDifferentOffset_ShouldMaintainInstant() {
    ZoneId applicationZoneId = ZoneId.of("Europe/London");
    TimeProvider timeProvider = new TimeProvider(applicationZoneId);

    OffsetDateTime originalTime =
        OffsetDateTime.of(2023, 6, 15, 14, 30, 0, 0, ZoneOffset.of("+02:00"));

    OffsetDateTime result = timeProvider.toApplicationOffset(originalTime);

    assertThat(result.toInstant()).isEqualTo(originalTime.toInstant());
    assertThat(result.getOffset())
        .isEqualTo(applicationZoneId.getRules().getOffset(originalTime.toInstant()));
  }
}
