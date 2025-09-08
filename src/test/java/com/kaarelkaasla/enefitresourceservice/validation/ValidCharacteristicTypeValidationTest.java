package com.kaarelkaasla.enefitresourceservice.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.kaarelkaasla.enefitresourceservice.entities.CharacteristicType;

public class ValidCharacteristicTypeValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  private static class TestClass {
    @ValidCharacteristicType private CharacteristicType characteristicType;

    public TestClass(CharacteristicType characteristicType) {
      this.characteristicType = characteristicType;
    }
  }

  @Test
  void validateCharacteristicType_ConsumptionType_Valid() {
    TestClass testObj = new TestClass(CharacteristicType.CONSUMPTION_TYPE);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void validateCharacteristicType_ChargingPoint_Valid() {
    TestClass testObj = new TestClass(CharacteristicType.CHARGING_POINT);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void validateCharacteristicType_ConnectionPointStatus_Valid() {
    TestClass testObj = new TestClass(CharacteristicType.CONNECTION_POINT_STATUS);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void validateCharacteristicType_Null_Valid() {
    TestClass testObj = new TestClass(null);

    Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

    assertThat(violations).isEmpty();
  }

  @Test
  void validateCharacteristicType_AllEnumValues_Valid() {
    for (CharacteristicType type : CharacteristicType.values()) {
      TestClass testObj = new TestClass(type);

      Set<ConstraintViolation<TestClass>> violations = validator.validate(testObj);

      assertThat(violations).isEmpty();
    }
  }

  @Test
  void validator_IsValid_ForSupportedValues_True() {
    ValidCharacteristicTypeValidator validatorInstance = new ValidCharacteristicTypeValidator();

    boolean isValid = validatorInstance.isValid(null, null);
    assertThat(isValid).isTrue();

    assertThat(validatorInstance.isValid(CharacteristicType.CONSUMPTION_TYPE, null)).isTrue();
    assertThat(validatorInstance.isValid(CharacteristicType.CHARGING_POINT, null)).isTrue();
    assertThat(validatorInstance.isValid(CharacteristicType.CONNECTION_POINT_STATUS, null))
        .isTrue();
  }

  @Test
  void characteristicType_EnumValues_MatchExpectedSet() {
    CharacteristicType[] values = CharacteristicType.values();
    assertThat(values).hasSize(3);
    assertThat(values)
        .containsExactlyInAnyOrder(
            CharacteristicType.CONSUMPTION_TYPE,
            CharacteristicType.CHARGING_POINT,
            CharacteristicType.CONNECTION_POINT_STATUS);
  }

  @Test
  void validator_ErrorHandling_ForNullAndValidValues() {
    ValidCharacteristicTypeValidator validatorInstance = new ValidCharacteristicTypeValidator();

    for (CharacteristicType type : CharacteristicType.values()) {
      assertThat(validatorInstance.isValid(type, null)).isTrue();
    }

    assertThat(validatorInstance.isValid(null, null)).isTrue();
  }
}
