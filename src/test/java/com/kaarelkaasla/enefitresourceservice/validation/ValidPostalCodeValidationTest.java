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

public class ValidPostalCodeValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  private static class TestClass {
    @ValidPostalCode private String postalCode;

    public TestClass(String postalCode) {
      this.postalCode = postalCode;
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345", "00000", "99999", "54321"})
  void shouldAcceptValidPostalCodes(String postalCode) {
    TestClass testObj = new TestClass(postalCode);

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
      strings = {
        "1234", "123456", "ABCDE", "123AB", "12 34", "12-34", "123.45", "12345a", "a12345", "",
        " 12345", "12345 ", "12  45"
      })
  void shouldRejectInvalidPostalCodes(String postalCode) {
    TestClass testObj = new TestClass(postalCode);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .anyMatch(message -> message.contains("Invalid postal code"));
  }

  @Test
  void shouldProvideDetailedErrorMessage() {
    TestClass testObj = new TestClass("ABCDE");

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isNotEmpty();
    ConstraintViolation<TestClass> violation = violations.iterator().next();
    assertThat(violation.getMessage())
        .isEqualTo("Invalid postal code 'ABCDE'. Must be exactly 5 digits (0-9)");
  }

  @Test
  void shouldTestValidatorDirectly() {
    ValidPostalCodeValidator validatorInstance = new ValidPostalCodeValidator();

    assertThat(validatorInstance.isValid("12345", null)).isTrue();
    assertThat(validatorInstance.isValid("00000", null)).isTrue();
    assertThat(validatorInstance.isValid("99999", null)).isTrue();
    assertThat(validatorInstance.isValid(null, null)).isTrue();

    assertThat(validatorInstance.isValid("1234", null)).isFalse();
    assertThat(validatorInstance.isValid("123456", null)).isFalse();
    assertThat(validatorInstance.isValid("ABCDE", null)).isFalse();
    assertThat(validatorInstance.isValid("123AB", null)).isFalse();
    assertThat(validatorInstance.isValid("", null)).isFalse();
  }
}
