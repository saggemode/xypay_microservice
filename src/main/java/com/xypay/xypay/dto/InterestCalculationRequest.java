package com.xypay.xypay.dto;

import java.math.BigDecimal;

public class InterestCalculationRequest {
    private BigDecimal balance;
    private Integer days;
    private String currency;
    
    // Getters and setters
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public Integer getDays() {
        return days;
    }
    
    public void setDays(Integer days) {
        this.days = days;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}