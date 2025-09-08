package com.kaarelkaasla.enefitresourceservice.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link ValidPostalCode} enforcing a 5-digit format.
 * Treats null as valid and builds a custom violation message with the rejected value.
 */
public class ValidPostalCodeValidator implements ConstraintValidator<ValidPostalCode, String> {

  private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("^\\d{5}$");

  @Override
  public boolean isValid(String postalCode, ConstraintValidatorContext context) {
    if (postalCode == null) {
      return true;
    }

    if (!POSTAL_CODE_PATTERN.matcher(postalCode).matches()) {
      if (context != null) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                String.format(
                    "Invalid postal code '%s'. Must be exactly 5 digits (0-9)", postalCode))
            .addConstraintViolation();
      }
      return false;
    }

    return true;
  }
}
