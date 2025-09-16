package com.xypay.xypay.dto;

import com.xypay.xypay.domain.FixedSavingsPurpose;
import com.xypay.xypay.domain.FixedSavingsSource;
import com.xypay.xypay.validation.FixedSavingsValidation;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@FixedSavingsValidation
public class FixedSavingsAccountCreateDTO {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum fixed savings amount is ₦1,000")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Source is required")
    private FixedSavingsSource source;
    
    @NotNull(message = "Purpose is required")
    private FixedSavingsPurpose purpose;
    
    private String purposeDescription;
    
    @NotNull(message = "Start date is required")
    @Future(message = "Start date cannot be in the past")
    private LocalDate startDate;
    
    @NotNull(message = "Payback date is required")
    @Future(message = "Payback date must be in the future")
    private LocalDate paybackDate;
    
    private Boolean autoRenewalEnabled = false;
    
    // Validation method
    public void validate() {
        if (startDate != null && paybackDate != null) {
            if (paybackDate.isBefore(startDate) || paybackDate.isEqual(startDate)) {
                throw new IllegalArgumentException("Payback date must be after start date");
            }
            
            long durationDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, paybackDate);
            if (durationDays < 7) {
                throw new IllegalArgumentException("Minimum duration is 7 days");
            }
            if (durationDays > 1000) {
                throw new IllegalArgumentException("Maximum duration is 1000 days");
            }
        }
    }
}