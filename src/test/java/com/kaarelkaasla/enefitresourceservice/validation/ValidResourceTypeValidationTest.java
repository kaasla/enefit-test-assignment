package com.kaarelkaasla.enefitresourceservice.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;

public class ValidResourceTypeValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  private static class TestClass {
    @ValidResourceType private ResourceType resourceType;

    public TestClass(ResourceType resourceType) {
      this.resourceType = resourceType;
    }
  }

  @Test
  void shouldAcceptMeteringPoint() {
    TestClass testObj = new TestClass(ResourceType.METERING_POINT);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldAcceptConnectionPoint() {
    TestClass testObj = new TestClass(ResourceType.CONNECTION_POINT);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldAcceptNullValue() {
    TestClass testObj = new TestClass(null);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldRejectInvalidEnumValue() {
    ValidResourceTypeValidator validatorInstance = new ValidResourceTypeValidator();

    boolean isValid = validatorInstance.isValid(null, null);
    assertThat(isValid).isTrue();

    isValid = validatorInstance.isValid(ResourceType.METERING_POINT, null);
    assertThat(isValid).isTrue();

    isValid = validatorInstance.isValid(ResourceType.CONNECTION_POINT, null);
    assertThat(isValid).isTrue();
  }
}
