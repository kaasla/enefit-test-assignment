package com.kaarelkaasla.enefitresourceservice.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchemaMigrationsTest {

  private String readClasspath(String path) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
      assertThat(is).withFailMessage("Resource not found: %s", path).isNotNull();
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Test
  @DisplayName("Baseline migration defines final schema and constraints")
  void baselineMigration_DefinesSchemaAndConstraints() throws IOException {
    String v1 = readClasspath("db/migration/V1__Create_resource_management_tables.sql");

    assertThat(v1).contains("CHECK (type IN ('METERING_POINT', 'CONNECTION_POINT'))");
    assertThat(v1).contains("country_code VARCHAR(2) NOT NULL CHECK (country_code ~ '^[A-Z]{2}$')");
    assertThat(v1).contains("created_at TIMESTAMPTZ");
    assertThat(v1).contains("updated_at TIMESTAMPTZ");

    assertThat(v1).contains("postal_code VARCHAR(5) NOT NULL");
    assertThat(v1).contains("CHECK (postal_code ~ '^[0-9]{5}$')");

    assertThat(v1)
        .contains(
            "CHECK (type IN ('CONSUMPTION_TYPE', 'CHARGING_POINT', 'CONNECTION_POINT_STATUS'))");
    assertThat(v1).contains("code VARCHAR(5) NOT NULL");
  }
}
