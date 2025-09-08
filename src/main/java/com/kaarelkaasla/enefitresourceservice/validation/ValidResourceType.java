package com.kaarelkaasla.enefitresourceservice.validation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidResourceTypeValidator.class)
@Documented
public @interface ValidResourceType {
  String message() default
      "Invalid resource type. Must be either METERING_POINT or CONNECTION_POINT";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
