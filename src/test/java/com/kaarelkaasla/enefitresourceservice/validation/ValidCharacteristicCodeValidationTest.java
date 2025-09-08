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

public class ValidCharacteristicCodeValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  private static class TestClass {
    @ValidCharacteristicCode private String code;

    public TestClass(String code) {
      this.code = code;
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"A", "AB", "ABC", "ABCD", "ABCDE", "12345", "CODE1", "1"})
  void shouldAcceptValidCharacteristicCodes(String code) {
    TestClass testObj = new TestClass(code);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldAcceptNullValue() {
    TestClass testObj = new TestClass(null);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"ABCDEF", "ABCDEFG", "TOOLONGCODE", "123456789", "VERYLONGCHARACTERISTICCODE"})
  void shouldRejectTooLongCharacteristicCodes(String code) {
    TestClass testObj = new TestClass(code);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .anyMatch(message -> message.contains("Invalid characteristic code"));
  }

  @Test
  void shouldProvideDetailedErrorMessage() {
    String longCode = "TOOLONG";
    TestClass testObj = new TestClass(longCode);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isNotEmpty();
    ConstraintViolation<TestClass> violation = violations.iterator().next();
    assertThat(violation.getMessage())
        .isEqualTo(
            String.format(
                "Invalid characteristic code '%s'. Must be maximum 5 characters, but was %d",
                longCode, longCode.length()));
  }

  @Test
  void shouldTestValidatorDirectly() {
    ValidCharacteristicCodeValidator validatorInstance = new ValidCharacteristicCodeValidator();

    assertThat(validatorInstance.isValid("A", null)).isTrue();
    assertThat(validatorInstance.isValid("ABCDE", null)).isTrue();
    assertThat(validatorInstance.isValid("12345", null)).isTrue();
    assertThat(validatorInstance.isValid("", null)).isTrue();
    assertThat(validatorInstance.isValid(null, null)).isTrue();

    assertThat(validatorInstance.isValid("ABCDEF", null)).isFalse();
    assertThat(validatorInstance.isValid("TOOLONG", null)).isFalse();
    assertThat(validatorInstance.isValid("123456", null)).isFalse();
  }

  @Test
  void shouldAcceptExactlyFiveCharacters() {
    TestClass testObj = new TestClass("ABCDE");

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldAcceptEmptyString() {
    TestClass testObj = new TestClass("");

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }
}
