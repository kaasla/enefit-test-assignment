package com.kaarelkaasla.enefitresourceservice.validation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCharacteristicCodeValidator.class)
@Documented
public @interface ValidCharacteristicCode {
  String message() default "Characteristic code must be maximum 5 characters";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
