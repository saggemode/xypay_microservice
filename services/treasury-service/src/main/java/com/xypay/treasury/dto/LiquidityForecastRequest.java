package com.xypay.treasury.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LiquidityForecastRequest {
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode;
    
    @NotNull(message = "Forecast days is required")
    @Min(value = 1, message = "Forecast days must be at least 1")
    @Max(value = 365, message = "Forecast days cannot exceed 365")
    private Integer forecastDays;
    
    private BigDecimal minimumRequiredLiquidity;
    
    private BigDecimal riskTolerance;
}
