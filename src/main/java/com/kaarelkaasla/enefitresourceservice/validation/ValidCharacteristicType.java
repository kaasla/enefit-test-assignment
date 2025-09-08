package com.kaarelkaasla.enefitresourceservice.validation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCharacteristicTypeValidator.class)
@Documented
public @interface ValidCharacteristicType {
  String message() default
      "Invalid characteristic type. Must be one of: CONSUMPTION_TYPE, CHARGING_POINT,"
          + " CONNECTION_POINT_STATUS";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
