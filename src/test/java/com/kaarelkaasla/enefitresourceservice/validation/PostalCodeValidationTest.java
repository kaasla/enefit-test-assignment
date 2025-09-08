package com.kaarelkaasla.enefitresourceservice.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.kaarelkaasla.enefitresourceservice.dtos.LocationRequest;

public class PostalCodeValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void postalCode_Valid_Accepted() {
    LocationRequest location = new LocationRequest("123 Main St", "New York", "12345", "US");

    Set<ConstraintViolation<LocationRequest>> violations = validator.validate(location);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345", "00000", "99999", "54321"})
  void postalCode_ValidSamples_Accepted(String postalCode) {
    LocationRequest location = new LocationRequest("123 Main St", "New York", postalCode, "US");

    Set<ConstraintViolation<LocationRequest>> violations = validator.validate(location);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1234", "123456", "ABCDE", "123AB", "12 34", "12-34", "123.45", "12345a", "a12345", "",
        " 12345", "12345 ", "12  45"
      })
  void postalCode_InvalidSamples_Rejected(String postalCode) {
    LocationRequest location = new LocationRequest("123 Main St", "New York", postalCode, "US");

    Set<ConstraintViolation<LocationRequest>> violations = validator.validate(location);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .anyMatch(message -> message.contains("Postal code must be exactly 5 digits"));
  }

  @Test
  void postalCode_Null_Rejected() {
    LocationRequest location = new LocationRequest("123 Main St", "New York", null, "US");

    Set<ConstraintViolation<LocationRequest>> violations = validator.validate(location);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .anyMatch(message -> message.contains("Postal code is required"));
  }

  @Test
  void postalCode_Blank_Rejected() {
    LocationRequest location = new LocationRequest("123 Main St", "New York", "   ", "US");

    Set<ConstraintViolation<LocationRequest>> violations = validator.validate(location);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .anyMatch(message -> message.contains("Postal code is required"));
  }
}
