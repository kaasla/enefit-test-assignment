package com.kaarelkaasla.enefitresourceservice.validation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchingCountryCodeValidator.class)
@Documented
public @interface MatchingCountryCode {
  String message() default "Resource country code must match location country code";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
