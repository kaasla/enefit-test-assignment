package com.kaarelkaasla.enefitresourceservice.services;

import java.time.*;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Provides application-zone timestamps and conversions.
 * Uses the ZoneId from TimeConfig for now() and converts arbitrary OffsetDateTime values to
 * that zone to keep timestamps consistent across the app.
 */
@Component
@RequiredArgsConstructor
public class TimeProvider {
  private final ZoneId applicationZoneId;

  public OffsetDateTime now() {
    return OffsetDateTime.now(applicationZoneId);
  }

  public OffsetDateTime toApplicationOffset(OffsetDateTime value) {
    if (value == null) return null;
    Instant instant = value.toInstant();
    return instant.atZone(applicationZoneId).toOffsetDateTime();
  }
}
