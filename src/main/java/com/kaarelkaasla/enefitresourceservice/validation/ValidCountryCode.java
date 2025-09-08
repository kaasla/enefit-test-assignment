package com.kaarelkaasla.enefitresourceservice.validation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCountryCodeValidator.class)
@Documented
public @interface ValidCountryCode {
  String message() default "Invalid country code. Must be a valid ISO 3166-1 alpha-2 code";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
