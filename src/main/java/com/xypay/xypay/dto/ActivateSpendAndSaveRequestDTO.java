package com.xypay.xypay.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivateSpendAndSaveRequestDTO {
    
    @NotNull(message = "Savings percentage is required")
    @DecimalMin(value = "1.0", message = "Savings percentage must be at least 1%")
    @DecimalMax(value = "50.0", message = "Savings percentage must not exceed 50%")
    @Digits(integer = 3, fraction = 2, message = "Savings percentage must have at most 3 integer digits and 2 decimal places")
    private BigDecimal savingsPercentage;
    
    @NotBlank(message = "Fund source is required")
    @Pattern(regexp = "^(wallet|xysave|both)$", message = "Fund source must be 'wallet', 'xysave', or 'both'")
    private String fundSource = "wallet";
    
    @DecimalMin(value = "0.01", message = "Initial amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Initial amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal initialAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.01", message = "Wallet amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Wallet amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal walletAmount;
    
    @DecimalMin(value = "0.01", message = "XySave amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "XySave amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal xySaveAmount;
    
    @NotNull(message = "Terms acceptance is required")
    @AssertTrue(message = "You must accept the terms and conditions to activate Spend and Save")
    private Boolean termsAccepted;
}
