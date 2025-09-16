package com.xypay.xypay.dto;

import com.xypay.xypay.domain.TargetSavingCategory;
import com.xypay.xypay.domain.TargetSavingFrequency;
import com.xypay.xypay.domain.TargetSavingSource;
import com.xypay.xypay.validation.ValidTargetSavingDates;
import com.xypay.xypay.validation.ValidTargetSavingFrequency;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ValidTargetSavingDates
@ValidTargetSavingFrequency
public class TargetSavingCreateRequestDTO {
    
    @NotBlank(message = "Target name is required")
    @Size(min = 3, max = 255, message = "Target name must be between 3 and 255 characters")
    private String name;
    
    @NotNull(message = "Category is required")
    private TargetSavingCategory category;
    
    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    @DecimalMax(value = "1000000000", message = "Target amount is too high")
    private BigDecimal targetAmount;
    
    @NotNull(message = "Frequency is required")
    private TargetSavingFrequency frequency;
    
    @NotNull(message = "Source is required")
    private TargetSavingSource source;
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    private String preferredDepositDay;
    
    private Boolean strictMode = false;
}
