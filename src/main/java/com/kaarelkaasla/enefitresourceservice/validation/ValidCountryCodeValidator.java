package com.kaarelkaasla.enefitresourceservice.validation;

import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link ValidCountryCode} that checks ISO country codes.
 * Treats null as valid (presence enforced elsewhere) and emits a detailed message.
 */
public class ValidCountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

  private static final Set<String> VALID_COUNTRY_CODES = Set.of(Locale.getISOCountries());

  @Override
  public boolean isValid(String countryCode, ConstraintValidatorContext context) {
    if (countryCode == null) {
      return true;
    }

    if (!VALID_COUNTRY_CODES.contains(countryCode)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "Invalid country code '%s'. Must be a valid ISO 3166-1 alpha-2 code",
                  countryCode))
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
