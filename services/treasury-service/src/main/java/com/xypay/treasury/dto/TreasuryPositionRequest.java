package com.xypay.treasury.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TreasuryPositionRequest {
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode;
    
    @NotNull(message = "Position amount is required")
    @DecimalMin(value = "0.01", message = "Position amount must be greater than 0")
    private BigDecimal positionAmount;
    
    private BigDecimal availableAmount;
    
    private BigDecimal reservedAmount;
    
    @NotNull(message = "Value date is required")
    private LocalDate valueDate;
    
    private LocalDate maturityDate;
    
    @NotNull(message = "Position type is required")
    private String positionType;
    
    @NotNull(message = "Liquidity bucket is required")
    private String liquidityBucket;
    
    private BigDecimal interestRate;
    
    private String costCenter;
    
    private String profitCenter;
    
    private BigDecimal riskWeight;
    
    private Boolean isActive = true;
}
