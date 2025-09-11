package com.xypay.xypay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FixedSavingsInterestRateDTO {
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate paybackDate;
    private BigDecimal interestRate;
    private String maturityAmount;
    private String interestEarned;
    private Integer durationDays;

    // Getters and setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getPaybackDate() {
        return paybackDate;
    }

    public void setPaybackDate(LocalDate paybackDate) {
        this.paybackDate = paybackDate;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public String getMaturityAmount() {
        return maturityAmount;
    }

    public void setMaturityAmount(String maturityAmount) {
        this.maturityAmount = maturityAmount;
    }

    public String getInterestEarned() {
        return interestEarned;
    }

    public void setInterestEarned(String interestEarned) {
        this.interestEarned = interestEarned;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
}