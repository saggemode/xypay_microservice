package com.xypay.xypay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SpendAndSaveStatsDTO {
    private Long activeAccounts;
    private BigDecimal totalSavings;
    private BigDecimal averageInterestRate;
    private BigDecimal monthlyGrowth;
    private Long newAccountsToday;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal interestPaid;
    private LocalDateTime lastUpdated;

    // Constructors
    public SpendAndSaveStatsDTO() {}

    public SpendAndSaveStatsDTO(Long activeAccounts, BigDecimal totalSavings, BigDecimal averageInterestRate, 
                               BigDecimal monthlyGrowth, Long newAccountsToday, BigDecimal totalDeposits, 
                               BigDecimal totalWithdrawals, BigDecimal interestPaid) {
        this.activeAccounts = activeAccounts;
        this.totalSavings = totalSavings;
        this.averageInterestRate = averageInterestRate;
        this.monthlyGrowth = monthlyGrowth;
        this.newAccountsToday = newAccountsToday;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.interestPaid = interestPaid;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getActiveAccounts() {
        return activeAccounts;
    }

    public void setActiveAccounts(Long activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }

    public BigDecimal getAverageInterestRate() {
        return averageInterestRate;
    }

    public void setAverageInterestRate(BigDecimal averageInterestRate) {
        this.averageInterestRate = averageInterestRate;
    }

    public BigDecimal getMonthlyGrowth() {
        return monthlyGrowth;
    }

    public void setMonthlyGrowth(BigDecimal monthlyGrowth) {
        this.monthlyGrowth = monthlyGrowth;
    }

    public Long getNewAccountsToday() {
        return newAccountsToday;
    }

    public void setNewAccountsToday(Long newAccountsToday) {
        this.newAccountsToday = newAccountsToday;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(BigDecimal totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public BigDecimal getInterestPaid() {
        return interestPaid;
    }

    public void setInterestPaid(BigDecimal interestPaid) {
        this.interestPaid = interestPaid;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
