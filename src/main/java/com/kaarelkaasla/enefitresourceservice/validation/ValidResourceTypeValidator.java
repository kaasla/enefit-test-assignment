package com.kaarelkaasla.enefitresourceservice.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;

/**
 * Validator for {@link ValidResourceType} ensuring enum values are allowed.
 * Treats null as valid and sets a descriptive violation message.
 */
public class ValidResourceTypeValidator
    implements ConstraintValidator<ValidResourceType, ResourceType> {

  private static final Set<ResourceType> VALID_RESOURCE_TYPES = Set.of(ResourceType.values());

  @Override
  public boolean isValid(ResourceType resourceType, ConstraintValidatorContext context) {
    if (resourceType == null) {
      return true;
    }

    if (!VALID_RESOURCE_TYPES.contains(resourceType)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "Invalid resource type '%s'. Must be either METERING_POINT or CONNECTION_POINT",
                  resourceType))
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
