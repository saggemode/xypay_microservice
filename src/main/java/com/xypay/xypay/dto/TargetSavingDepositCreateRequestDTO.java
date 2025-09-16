package com.xypay.xypay.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TargetSavingDepositCreateRequestDTO {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
    @DecimalMax(value = "100000000", message = "Deposit amount is too high")
    private BigDecimal amount;
    
    @Size(max = 1000, message = "Notes are too long (maximum 1000 characters)")
    private String notes;
}
