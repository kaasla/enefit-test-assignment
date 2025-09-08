package com.kaarelkaasla.enefitresourceservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.kaarelkaasla.enefitresourceservice.dtos.PatchResourceRequest;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceRequest;

/**
 * Validator for {@link MatchingCountryCode} across create/patch request DTOs.
 * Compares resource and location codes; ignores validation when either code is missing.
 */
public class MatchingCountryCodeValidator
    implements ConstraintValidator<MatchingCountryCode, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    String resourceCountryCode = null;
    String locationCountryCode = null;

    // Support both create and patch DTOs using pattern matching 'instanceof'
    if (value instanceof ResourceRequest request) {
      resourceCountryCode = request.countryCode();
      locationCountryCode = request.location() != null ? request.location().countryCode() : null;
    } else if (value instanceof PatchResourceRequest request) {
      resourceCountryCode = request.countryCode().orElse(null);
      locationCountryCode = request.location().map(loc -> loc.countryCode()).orElse(null);
    }

    // If either is absent, skip (other constraints handle presence/format)
    if (resourceCountryCode == null || locationCountryCode == null) {
      return true;
    }

    if (!resourceCountryCode.equals(locationCountryCode)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "Resource country code '%s' must match location country code '%s'",
                  resourceCountryCode, locationCountryCode))
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
