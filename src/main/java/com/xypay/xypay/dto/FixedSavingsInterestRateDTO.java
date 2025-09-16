package com.xypay.xypay.dto;

import com.xypay.xypay.validation.FixedSavingsValidation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@FixedSavingsValidation
public class FixedSavingsInterestRateDTO {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum amount is ₦1,000")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "Payback date is required")
    private LocalDate paybackDate;
    
    private BigDecimal interestRate;
    private String maturityAmount;
    private String interestEarned;
    private Integer durationDays;
}