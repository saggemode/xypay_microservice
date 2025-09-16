package com.xypay.xypay.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import com.xypay.xypay.dto.FixedSavingsAccountCreateDTO;
import com.xypay.xypay.dto.FixedSavingsInterestRateDTO;

import java.lang.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FixedSavingsValidation.FixedSavingsValidator.class)
@Documented
public @interface FixedSavingsValidation {
    
    String message() default "Invalid fixed savings data";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class FixedSavingsValidator implements ConstraintValidator<FixedSavingsValidation, Object> {
        
        @Override
        public void initialize(FixedSavingsValidation constraintAnnotation) {
        }
        
        @Override
        public boolean isValid(Object obj, ConstraintValidatorContext context) {
            boolean isValid = true;
            LocalDate startDate = null;
            LocalDate paybackDate = null;
            
            // Extract dates based on object type
            if (obj instanceof FixedSavingsAccountCreateDTO) {
                FixedSavingsAccountCreateDTO dto = (FixedSavingsAccountCreateDTO) obj;
                startDate = dto.getStartDate();
                paybackDate = dto.getPaybackDate();
            } else if (obj instanceof FixedSavingsInterestRateDTO) {
                FixedSavingsInterestRateDTO dto = (FixedSavingsInterestRateDTO) obj;
                startDate = dto.getStartDate();
                paybackDate = dto.getPaybackDate();
            }
            
            if (startDate != null && paybackDate != null) {
                // Check if payback date is after start date
                if (!paybackDate.isAfter(startDate)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Payback date must be after start date")
                           .addPropertyNode("paybackDate")
                           .addConstraintViolation();
                    isValid = false;
                }
                
                // Check duration constraints
                long durationDays = ChronoUnit.DAYS.between(startDate, paybackDate);
                if (durationDays < 7) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Minimum duration is 7 days")
                           .addPropertyNode("paybackDate")
                           .addConstraintViolation();
                    isValid = false;
                }
                if (durationDays > 1000) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Maximum duration is 1000 days")
                           .addPropertyNode("paybackDate")
                           .addConstraintViolation();
                    isValid = false;
                }
            }
            
            // Check if start date is not in the past
            if (startDate != null && startDate.isBefore(LocalDate.now())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start date cannot be in the past")
                       .addPropertyNode("startDate")
                       .addConstraintViolation();
                isValid = false;
            }
            
            return isValid;
        }
    }
}
