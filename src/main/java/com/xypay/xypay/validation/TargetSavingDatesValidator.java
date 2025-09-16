package com.xypay.xypay.validation;

import com.xypay.xypay.dto.TargetSavingCreateRequestDTO;
import com.xypay.xypay.dto.TargetSavingUpdateRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TargetSavingDatesValidator implements ConstraintValidator<ValidTargetSavingDates, Object> {
    
    @Override
    public void initialize(ValidTargetSavingDates constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        if (obj instanceof TargetSavingCreateRequestDTO) {
            TargetSavingCreateRequestDTO dto = (TargetSavingCreateRequestDTO) obj;
            startDate = dto.getStartDate();
            endDate = dto.getEndDate();
        } else if (obj instanceof TargetSavingUpdateRequestDTO) {
            TargetSavingUpdateRequestDTO dto = (TargetSavingUpdateRequestDTO) obj;
            startDate = dto.getStartDate();
            endDate = dto.getEndDate();
        }
        
        if (startDate == null || endDate == null) {
            return true; // Let other validators handle null checks
        }
        
        // Check if end date is after start date
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("End date must be after start date")
                   .addPropertyNode("endDate")
                   .addConstraintViolation();
            return false;
        }
        
        // Check if the date range is reasonable (not too long)
        long daysDiff = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysDiff > 3650) { // 10 years
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Target period cannot exceed 10 years")
                   .addPropertyNode("endDate")
                   .addConstraintViolation();
            return false;
        }
        
        if (daysDiff < 1) { // At least 1 day
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Target period must be at least 1 day")
                   .addPropertyNode("endDate")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
