package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class InterestForecastDTO {
    private BigDecimal dailyInterest;
    private BigDecimal monthlyInterest;
    private BigDecimal annualInterest;
    private Integer forecastDays;
    private BigDecimal currentBalance;
    private Map<String, Object> interestBreakdown;
}
