package com.xypay.xypay.dto;

import com.xypay.xypay.domain.XySaveInvestment;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class XySaveInvestmentCreateRequestDTO {
    
    @NotNull(message = "Investment type is required")
    private XySaveInvestment.InvestmentType investmentType;
    
    @NotNull(message = "Amount invested is required")
    @DecimalMin(value = "1000.00", message = "Minimum investment amount is ₦1,000")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 integer digits and 2 decimal places")
    private BigDecimal amountInvested;
    
    @NotNull(message = "Expected return rate is required")
    @DecimalMin(value = "0.00", message = "Expected return rate must be non-negative")
    @DecimalMax(value = "100.00", message = "Expected return rate must be at most 100%")
    @Digits(integer = 3, fraction = 2, message = "Expected return rate must have at most 3 integer digits and 2 decimal places")
    private BigDecimal expectedReturnRate;
    
    private LocalDate maturityDate;
}
