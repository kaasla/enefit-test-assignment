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

import com.kaarelkaasla.enefitresourceservice.dtos.CharacteristicRequest;
import com.kaarelkaasla.enefitresourceservice.entities.CharacteristicType;

public class CharacteristicCodeValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @ParameterizedTest
  @ValueSource(strings = {"A", "AB", "ABC", "ABCD", "ABCDE"})
  void characteristicCode_ValidLengths_Accepted(String code) {
    CharacteristicRequest characteristic =
        new CharacteristicRequest(code, CharacteristicType.CONSUMPTION_TYPE, "test-value");

    Set<ConstraintViolation<CharacteristicRequest>> violations = validator.validate(characteristic);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345", "CODE1", "TEST1"})
  void characteristicCode_ValidFiveCharacters_Accepted(String code) {
    CharacteristicRequest characteristic =
        new CharacteristicRequest(code, CharacteristicType.CONSUMPTION_TYPE, "test-value");

    Set<ConstraintViolation<CharacteristicRequest>> violations = validator.validate(characteristic);

    assertThat(violations).isEmpty();
  }

  @Test
  void characteristicCode_TooLong_Rejected() {
    CharacteristicRequest characteristic =
        new CharacteristicRequest("ABCDEF", CharacteristicType.CONSUMPTION_TYPE, "test-value");

    Set<ConstraintViolation<CharacteristicRequest>> violations = validator.validate(characteristic);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsAnyOf("Code must be maximum 5 characters");
  }

  @ParameterizedTest
  @ValueSource(strings = {"ABCDEF", "ABCDEFG", "TOOLONGCODE", "123456789"})
  void characteristicCode_LongerThanFive_Rejected(String code) {
    CharacteristicRequest characteristic =
        new CharacteristicRequest(code, CharacteristicType.CONSUMPTION_TYPE, "test-value");

    Set<ConstraintViolation<CharacteristicRequest>> violations = validator.validate(characteristic);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsAnyOf("Code must be maximum 5 characters");
  }

  @Test
  void characteristicCode_Null_Rejected() {
    CharacteristicRequest characteristic =
        new CharacteristicRequest(null, CharacteristicType.CONSUMPTION_TYPE, "test-value");

    Set<ConstraintViolation<CharacteristicRequest>> violations = validator.validate(characteristic);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsAnyOf("Code is required");
  }

  @Test
  void characteristicCode_Empty_Rejected() {
    CharacteristicRequest characteristic =
        new CharacteristicRequest("", CharacteristicType.CONSUMPTION_TYPE, "test-value");

    Set<ConstraintViolation<CharacteristicRequest>> violations = validator.validate(characteristic);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsAnyOf("Code is required");
  }

  @Test
  void characteristicCode_Blank_Rejected() {
    CharacteristicRequest characteristic =
        new CharacteristicRequest("   ", CharacteristicType.CONSUMPTION_TYPE, "test-value");

    Set<ConstraintViolation<CharacteristicRequest>> violations = validator.validate(characteristic);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsAnyOf("Code is required");
  }

  @Test
  void characteristic_MultipleInvalidFields_AllViolationsReported() {
    CharacteristicRequest characteristic = new CharacteristicRequest("TOOLONG", null, "");

    Set<ConstraintViolation<CharacteristicRequest>> violations = validator.validate(characteristic);

    assertThat(violations).hasSize(4);
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains(
            "Code must be maximum 5 characters",
            "Invalid characteristic code 'TOOLONG'. Must be maximum 5 characters, but was 7",
            "Type is required",
            "Value is required");
  }
}
