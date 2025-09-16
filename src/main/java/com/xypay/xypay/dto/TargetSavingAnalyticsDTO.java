package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TargetSavingAnalyticsDTO {
    
    private Integer totalDeposits;
    private BigDecimal averageDeposit;
    private Double depositFrequency;
    private BigDecimal progressPercentage;
    private BigDecimal remainingAmount;
    private Integer daysRemaining;
    private Boolean isOverdue;
    private BigDecimal dailyTarget;
    private BigDecimal weeklyTarget;
    private BigDecimal monthlyTarget;
}
