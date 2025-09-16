package com.xypay.xypay.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawFromSpendAndSaveRequestDTO {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal amount;
    
    @NotBlank(message = "Destination is required")
    @Pattern(regexp = "^(wallet|xysave)$", message = "Destination must be 'wallet' or 'xysave'")
    private String destination = "wallet";
}
