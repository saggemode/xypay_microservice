package com.xypay.xypay.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = com.xypay.xypay.validation.TargetSavingFrequencyValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTargetSavingFrequency {
    String message() default "Preferred deposit day is required for weekly/monthly frequency";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
