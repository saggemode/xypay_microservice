package com.xypay.xypay.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class XySaveDepositRequestDTO {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    private String description = "Deposit to XySave";
}
