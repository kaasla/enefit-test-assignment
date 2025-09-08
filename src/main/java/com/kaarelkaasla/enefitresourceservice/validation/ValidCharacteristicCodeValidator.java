package com.kaarelkaasla.enefitresourceservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link ValidCharacteristicCode} enforcing max length.
 * Treats null as valid and emits a detailed violation including actual length.
 */
public class ValidCharacteristicCodeValidator
    implements ConstraintValidator<ValidCharacteristicCode, String> {

  private static final int MAX_LENGTH = 5;

  @Override
  public boolean isValid(String code, ConstraintValidatorContext context) {
    if (code == null) {
      return true;
    }

    if (code.length() > MAX_LENGTH) {
      if (context != null) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                String.format(
                    "Invalid characteristic code '%s'. Must be maximum %d characters, but was %d",
                    code, MAX_LENGTH, code.length()))
            .addConstraintViolation();
      }
      return false;
    }

    return true;
  }
}
