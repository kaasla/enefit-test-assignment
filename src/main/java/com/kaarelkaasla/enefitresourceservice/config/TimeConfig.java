package com.kaarelkaasla.enefitresourceservice.config;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the application's ZoneId based on configuration.
 * Uses 'app.timezone' or the system default (including 'SYSTEM' alias).
 */
@Configuration
public class TimeConfig {

  @Bean
  public ZoneId applicationZoneId(@Value("${app.timezone:}") String tz) {
    if (tz == null || tz.isBlank()) {
      return ZoneId.systemDefault();
    }
    if ("SYSTEM".equalsIgnoreCase(tz)) {
      return ZoneId.systemDefault();
    }
    return ZoneId.of(tz);
  }
}
