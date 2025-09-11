package com.xypay.xypay.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class InterestCalculationResponse {
    private BigDecimal balance;
    private int calculationPeriodDays;
    private BigDecimal totalInterest;
    private BigDecimal effectiveRate;
    private List<Map<String, Object>> breakdown;
    
    // Getters and setters
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public int getCalculationPeriodDays() {
        return calculationPeriodDays;
    }
    
    public void setCalculationPeriodDays(int calculationPeriodDays) {
        this.calculationPeriodDays = calculationPeriodDays;
    }
    
    public BigDecimal getTotalInterest() {
        return totalInterest;
    }
    
    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }
    
    public BigDecimal getEffectiveRate() {
        return effectiveRate;
    }
    
    public void setEffectiveRate(BigDecimal effectiveRate) {
        this.effectiveRate = effectiveRate;
    }
    
    public List<Map<String, Object>> getBreakdown() {
        return breakdown;
    }
    
    public void setBreakdown(List<Map<String, Object>> breakdown) {
        this.breakdown = breakdown;
    }
}