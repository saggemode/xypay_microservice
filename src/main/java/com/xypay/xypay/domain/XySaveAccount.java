package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "xy_save_accounts")
public class XySaveAccount extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "account_number", unique = true, length = 20)
    private String accountNumber;
    
    @Column(name = "balance", precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "total_interest_earned", precision = 19, scale = 4)
    private BigDecimal totalInterestEarned = BigDecimal.ZERO;
    
    @Column(name = "last_interest_calculation")
    private LocalDateTime lastInterestCalculation;
    
    @Column(name = "daily_interest_rate", precision = 5, scale = 4)
    private BigDecimal dailyInterestRate = new BigDecimal("0.0004"); // 0.04% daily = ~15% annual
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "auto_save_enabled")
    private Boolean autoSaveEnabled = false;
    
    @Column(name = "auto_save_percentage", precision = 5, scale = 2)
    private BigDecimal autoSavePercentage = new BigDecimal("10.00"); // 10% by default
    
    @Column(name = "auto_save_min_amount", precision = 19, scale = 4)
    private BigDecimal autoSaveMinAmount = new BigDecimal("100.00");
    
    // Constructors
    public XySaveAccount() {}
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public BigDecimal getTotalInterestEarned() {
        return totalInterestEarned;
    }
    
    public void setTotalInterestEarned(BigDecimal totalInterestEarned) {
        this.totalInterestEarned = totalInterestEarned;
    }
    
    public LocalDateTime getLastInterestCalculation() {
        return lastInterestCalculation;
    }
    
    public void setLastInterestCalculation(LocalDateTime lastInterestCalculation) {
        this.lastInterestCalculation = lastInterestCalculation;
    }
    
    public BigDecimal getDailyInterestRate() {
        return dailyInterestRate;
    }
    
    public void setDailyInterestRate(BigDecimal dailyInterestRate) {
        this.dailyInterestRate = dailyInterestRate;
    }
    
    public Boolean getActive() {
        return isActive;
    }
    
    public void setActive(Boolean active) {
        isActive = active;
    }
    
    public Boolean getAutoSaveEnabled() {
        return autoSaveEnabled;
    }
    
    public void setAutoSaveEnabled(Boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }
    
    public BigDecimal getAutoSavePercentage() {
        return autoSavePercentage;
    }
    
    public void setAutoSavePercentage(BigDecimal autoSavePercentage) {
        this.autoSavePercentage = autoSavePercentage;
    }
    
    public BigDecimal getAutoSaveMinAmount() {
        return autoSaveMinAmount;
    }
    
    public void setAutoSaveMinAmount(BigDecimal autoSaveMinAmount) {
        this.autoSaveMinAmount = autoSaveMinAmount;
    }
}