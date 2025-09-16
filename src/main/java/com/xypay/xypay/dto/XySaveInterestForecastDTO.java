package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class XySaveInterestForecastDTO {
    
    private BigDecimal dailyInterest;
    private BigDecimal weeklyInterest;
    private BigDecimal monthlyInterest;
    private BigDecimal yearlyInterest;
    private BigDecimal annualRate;
    private BigDecimal currentBalance;
    private BigDecimal totalInterestEarned;
}
