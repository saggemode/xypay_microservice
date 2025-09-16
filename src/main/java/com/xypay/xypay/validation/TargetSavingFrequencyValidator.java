package com.xypay.xypay.validation;

import com.xypay.xypay.domain.TargetSavingFrequency;
import com.xypay.xypay.dto.TargetSavingCreateRequestDTO;
import com.xypay.xypay.dto.TargetSavingUpdateRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TargetSavingFrequencyValidator implements ConstraintValidator<ValidTargetSavingFrequency, Object> {
    
    @Override
    public void initialize(ValidTargetSavingFrequency constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }
        
        TargetSavingFrequency frequency = null;
        String preferredDepositDay = null;
        
        if (obj instanceof TargetSavingCreateRequestDTO) {
            TargetSavingCreateRequestDTO dto = (TargetSavingCreateRequestDTO) obj;
            frequency = dto.getFrequency();
            preferredDepositDay = dto.getPreferredDepositDay();
        } else if (obj instanceof TargetSavingUpdateRequestDTO) {
            TargetSavingUpdateRequestDTO dto = (TargetSavingUpdateRequestDTO) obj;
            frequency = dto.getFrequency();
            preferredDepositDay = dto.getPreferredDepositDay();
        }
        
        if (frequency == null) {
            return true; // Let other validators handle null checks
        }
        
        // Check if preferred deposit day is required for weekly/monthly frequency
        if ((frequency == TargetSavingFrequency.WEEKLY || frequency == TargetSavingFrequency.MONTHLY)) {
            if (preferredDepositDay == null || preferredDepositDay.trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Preferred deposit day is required for weekly/monthly frequency")
                       .addPropertyNode("preferredDepositDay")
                       .addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}
