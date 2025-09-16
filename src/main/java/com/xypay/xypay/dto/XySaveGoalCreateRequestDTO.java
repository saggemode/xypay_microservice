package com.xypay.xypay.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class XySaveGoalCreateRequestDTO {
    
    @NotBlank(message = "Goal name is required")
    private String name;
    
    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Target amount must have at most 15 integer digits and 2 decimal places")
    private BigDecimal targetAmount;
    
    private LocalDate targetDate;
}
