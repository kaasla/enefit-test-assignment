package com.kaarelkaasla.enefitresourceservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class TimeConfigTest {

  private final TimeConfig timeConfig = new TimeConfig();

  @Test
  void applicationZoneId_WithNullTimezone_ShouldReturnSystemDefault() {
    ZoneId result = timeConfig.applicationZoneId(null);
    assertThat(result).isEqualTo(ZoneId.systemDefault());
  }

  @Test
  void applicationZoneId_WithEmptyTimezone_ShouldReturnSystemDefault() {
    ZoneId result = timeConfig.applicationZoneId("");
    assertThat(result).isEqualTo(ZoneId.systemDefault());
  }

  @Test
  void applicationZoneId_WithBlankTimezone_ShouldReturnSystemDefault() {
    ZoneId result = timeConfig.applicationZoneId("   ");
    assertThat(result).isEqualTo(ZoneId.systemDefault());
  }

  @Test
  void applicationZoneId_WithSystemKeyword_ShouldReturnSystemDefault() {
    ZoneId result = timeConfig.applicationZoneId("SYSTEM");
    assertThat(result).isEqualTo(ZoneId.systemDefault());
  }

  @Test
  void applicationZoneId_WithSystemKeywordLowercase_ShouldReturnSystemDefault() {
    ZoneId result = timeConfig.applicationZoneId("system");
    assertThat(result).isEqualTo(ZoneId.systemDefault());
  }

  @Test
  void applicationZoneId_WithSystemKeywordMixedCase_ShouldReturnSystemDefault() {
    ZoneId result = timeConfig.applicationZoneId("SyStEm");
    assertThat(result).isEqualTo(ZoneId.systemDefault());
  }

  @Test
  void applicationZoneId_WithValidTimezone_ShouldReturnSpecifiedZone() {
    ZoneId result = timeConfig.applicationZoneId("America/New_York");
    assertThat(result).isEqualTo(ZoneId.of("America/New_York"));
  }

  @Test
  void applicationZoneId_WithUtcTimezone_ShouldReturnUtcZone() {
    ZoneId result = timeConfig.applicationZoneId("UTC");
    assertThat(result).isEqualTo(ZoneId.of("UTC"));
  }

  @Test
  void applicationZoneId_WithEuropeBerlinTimezone_ShouldReturnBerlinZone() {
    ZoneId result = timeConfig.applicationZoneId("Europe/Berlin");
    assertThat(result).isEqualTo(ZoneId.of("Europe/Berlin"));
  }
}
