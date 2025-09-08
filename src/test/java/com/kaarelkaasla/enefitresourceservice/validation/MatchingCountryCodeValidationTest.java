package com.kaarelkaasla.enefitresourceservice.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kaarelkaasla.enefitresourceservice.dtos.LocationRequest;
import com.kaarelkaasla.enefitresourceservice.dtos.PatchResourceRequest;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceRequest;
import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;

class MatchingCountryCodeValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @Test
  void createResourceRequest_whenCountryCodesMatch_shouldBeValid() {
    LocationRequest location = new LocationRequest("123 Main Street", "New York", "10001", "US");

    ResourceRequest request =
        new ResourceRequest(ResourceType.METERING_POINT, "US", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }

  @Test
  void createResourceRequest_whenCountryCodesDontMatch_shouldBeInvalid() {
    LocationRequest location = new LocationRequest("123 Main Street", "New York", "10001", "US");

    ResourceRequest request =
        new ResourceRequest(ResourceType.METERING_POINT, "DE", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations)
        .hasSize(1)
        .extracting(ConstraintViolation::getMessage)
        .anySatisfy(
            message ->
                assertThat(message)
                    .matches("Resource country code '.+' must match location country code '.+'"));
  }

  @Test
  void updateResourceRequest_whenCountryCodesMatch_shouldBeValid() {
    LocationRequest location = new LocationRequest("Unter den Linden 1", "Berlin", "10117", "DE");

    ResourceRequest request =
        new ResourceRequest(ResourceType.CONNECTION_POINT, "DE", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }

  @Test
  void updateResourceRequest_whenCountryCodesDontMatch_shouldBeInvalid() {
    LocationRequest location = new LocationRequest("Unter den Linden 1", "Berlin", "10117", "DE");

    ResourceRequest request =
        new ResourceRequest(ResourceType.CONNECTION_POINT, "FR", location, null);

    Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
    assertThat(violations)
        .hasSize(1)
        .extracting(ConstraintViolation::getMessage)
        .anySatisfy(
            message ->
                assertThat(message)
                    .matches("Resource country code '.+' must match location country code '.+'"));
  }

  @Test
  void patchResourceRequest_whenBothCountryCodesPresent_andMatch_shouldBeValid() {
    LocationRequest location = new LocationRequest("25 Rue de la Paix", "Paris", "75002", "FR");

    PatchResourceRequest request =
        new PatchResourceRequest(
            Optional.of(ResourceType.METERING_POINT),
            Optional.of("FR"),
            Optional.of(location),
            Optional.empty());

    Set<ConstraintViolation<PatchResourceRequest>> violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }

  @Test
  void patchResourceRequest_whenBothCountryCodesPresent_andDontMatch_shouldBeInvalid() {
    LocationRequest location = new LocationRequest("25 Rue de la Paix", "Paris", "75002", "FR");

    PatchResourceRequest request =
        new PatchResourceRequest(
            Optional.of(ResourceType.METERING_POINT),
            Optional.of("US"),
            Optional.of(location),
            Optional.empty());

    Set<ConstraintViolation<PatchResourceRequest>> violations = validator.validate(request);
    assertThat(violations)
        .hasSize(1)
        .extracting(ConstraintViolation::getMessage)
        .anySatisfy(
            message ->
                assertThat(message)
                    .matches("Resource country code '.+' must match location country code '.+'"));
  }

  @Test
  void patchResourceRequest_whenOnlyOneCountryCodePresent_shouldBeValid() {
    LocationRequest location = new LocationRequest("25 Rue de la Paix", "Paris", "75002", "FR");

    PatchResourceRequest request =
        new PatchResourceRequest(
            Optional.of(ResourceType.METERING_POINT),
            Optional.empty(),
            Optional.of(location),
            Optional.empty());

    Set<ConstraintViolation<PatchResourceRequest>> violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }

  @Test
  void patchResourceRequest_whenNoCountryCodesPresent_shouldBeValid() {
    PatchResourceRequest request =
        new PatchResourceRequest(
            Optional.of(ResourceType.METERING_POINT),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    Set<ConstraintViolation<PatchResourceRequest>> violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }
}
