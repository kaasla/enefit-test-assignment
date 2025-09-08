package com.kaarelkaasla.enefitresourceservice.validation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPostalCodeValidator.class)
@Documented
public @interface ValidPostalCode {
  String message() default "Postal code must be exactly 5 digits (0-9)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
