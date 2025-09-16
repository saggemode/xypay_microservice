package com.xypay.xypay.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class XySaveAutoSaveRequestDTO {
    
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
    
    @DecimalMin(value = "1.00", message = "Percentage must be at least 1%")
    @DecimalMax(value = "100.00", message = "Percentage must be at most 100%")
    @Digits(integer = 3, fraction = 2, message = "Percentage must have at most 3 integer digits and 2 decimal places")
    private BigDecimal percentage = new BigDecimal("10.00");
    
    @DecimalMin(value = "0.00", message = "Minimum amount must be non-negative")
    @Digits(integer = 15, fraction = 2, message = "Minimum amount must have at most 15 integer digits and 2 decimal places")
    private BigDecimal minAmount = new BigDecimal("100.00");
}
