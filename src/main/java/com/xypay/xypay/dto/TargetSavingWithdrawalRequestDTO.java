package com.xypay.xypay.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TargetSavingWithdrawalRequestDTO {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Withdrawal amount must be greater than zero")
    private BigDecimal amount;
    
    @NotBlank(message = "Destination is required")
    @Pattern(regexp = "^(wallet|xysave)$", message = "Destination must be either 'wallet' or 'xysave'")
    private String destination;
    
    @Size(max = 1000, message = "Notes are too long (maximum 1000 characters)")
    private String notes;
}
