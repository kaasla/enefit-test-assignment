package com.kaarelkaasla.enefitresourceservice.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kaarelkaasla.enefitresourceservice.dtos.LocationRequest;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceRequest;
import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;

class ValidCountryCodeValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @Test
  void createResourceRequest_ValidCountryCode_IsValid() {
    LocationRequest location = new LocationRequest("123 Main Street", "New York", "10001", "US");

    ResourceRequest request =
        new ResourceRequest(ResourceType.METERING_POINT, "US", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }

  @Test
  void createResourceRequest_InvalidResourceCountryCode_IsInvalid() {
    LocationRequest location = new LocationRequest("123 Main Street", "New York", "10001", "US");

    ResourceRequest request =
        new ResourceRequest(ResourceType.METERING_POINT, "XX", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(2);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains(
            "Invalid country code 'XX'. Must be a valid ISO 3166-1 alpha-2 code",
            "Resource country code 'XX' must match location country code 'US'");
  }

  @Test
  void createResourceRequest_InvalidLocationCountryCode_IsInvalid() {
    LocationRequest location = new LocationRequest("123 Main Street", "New York", "10001", "YY");

    ResourceRequest request =
        new ResourceRequest(ResourceType.METERING_POINT, "US", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(2);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains(
            "Invalid country code 'YY'. Must be a valid ISO 3166-1 alpha-2 code",
            "Resource country code 'US' must match location country code 'YY'");
  }

  @Test
  void createResourceRequest_BothCountryCodesInvalid_AllViolationsPresent() {
    LocationRequest location = new LocationRequest("123 Main Street", "New York", "10001", "YY");

    ResourceRequest request =
        new ResourceRequest(ResourceType.METERING_POINT, "XX", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(3);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains(
            "Invalid country code 'XX'. Must be a valid ISO 3166-1 alpha-2 code",
            "Invalid country code 'YY'. Must be a valid ISO 3166-1 alpha-2 code",
            "Resource country code 'XX' must match location country code 'YY'");
  }

  @Test
  void createResourceRequest_MismatchedCountryCodes_HasMatchingViolation() {
    LocationRequest location = new LocationRequest("Unter den Linden 1", "Berlin", "10117", "DE");

    ResourceRequest request =
        new ResourceRequest(ResourceType.METERING_POINT, "US", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations)
        .hasSize(1)
        .extracting(ConstraintViolation::getMessage)
        .contains("Resource country code 'US' must match location country code 'DE'");
  }

  @Test
  void locationRequest_ValidCountryCode_IsValid() {
    LocationRequest location = new LocationRequest("Champs-Élysées 1", "Paris", "75008", "FR");

    Set<ConstraintViolation<LocationRequest>> violations = validator.validate(location);
    assertThat(violations).isEmpty();
  }

  @Test
  void locationRequest_InvalidCountryCode_IsInvalid() {
    LocationRequest location = new LocationRequest("Some Street 1", "Some City", "12345", "ZZ");

    Set<ConstraintViolation<LocationRequest>> violations = validator.validate(location);
    assertThat(violations)
        .hasSize(1)
        .extracting(ConstraintViolation::getMessage)
        .contains("Invalid country code 'ZZ'. Must be a valid ISO 3166-1 alpha-2 code");
  }
}
