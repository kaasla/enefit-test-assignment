package com.kaarelkaasla.enefitresourceservice.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.kaarelkaasla.enefitresourceservice.entities.CharacteristicType;

/**
 * Validator for {@link ValidCharacteristicType} ensuring enum values are allowed.
 * Treats null as valid and sets a clear violation message.
 */
public class ValidCharacteristicTypeValidator
    implements ConstraintValidator<ValidCharacteristicType, CharacteristicType> {

  private static final Set<CharacteristicType> VALID_CHARACTERISTIC_TYPES =
      Set.of(CharacteristicType.values());

  @Override
  public boolean isValid(
      CharacteristicType characteristicType, ConstraintValidatorContext context) {
    if (characteristicType == null) {
      return true;
    }

    if (!VALID_CHARACTERISTIC_TYPES.contains(characteristicType)) {
      if (context != null) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                String.format(
                    "Invalid characteristic type '%s'. Must be one of: CONSUMPTION_TYPE,"
                        + " CHARGING_POINT, CONNECTION_POINT_STATUS",
                    characteristicType))
            .addConstraintViolation();
      }
      return false;
    }

    return true;
  }
}
