package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "spend_and_save_accounts")
public class SpendAndSaveAccount extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "account_number", unique = true, length = 20)
    private String accountNumber;
    
    @Column(name = "balance", precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "total_interest_earned", precision = 19, scale = 4)
    private BigDecimal totalInterestEarned = BigDecimal.ZERO;
    
    @Column(name = "total_saved_from_spending", precision = 19, scale = 4)
    private BigDecimal totalSavedFromSpending = BigDecimal.ZERO;
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @Column(name = "savings_percentage", precision = 5, scale = 2)
    private BigDecimal savingsPercentage = new BigDecimal("5.00");
    
    @Column(name = "min_transaction_amount", precision = 19, scale = 4)
    private BigDecimal minTransactionAmount = new BigDecimal("100.00");
    
    // Tiered interest rates
    @Column(name = "daily_tier_1_rate", precision = 8, scale = 6)
    private BigDecimal dailyTier1Rate = new BigDecimal("0.000548"); // 20% / 365
    
    @Column(name = "daily_tier_2_rate", precision = 8, scale = 6)
    private BigDecimal dailyTier2Rate = new BigDecimal("0.000438"); // 16% / 365
    
    @Column(name = "daily_tier_3_rate", precision = 8, scale = 6)
    private BigDecimal dailyTier3Rate = new BigDecimal("0.000219"); // 8% / 365
    
    @Column(name = "last_interest_calculation")
    private LocalDateTime lastInterestCalculation;
    
    @Column(name = "total_transactions_processed")
    private Integer totalTransactionsProcessed = 0;
    
    @Column(name = "last_auto_save_date")
    private LocalDate lastAutoSaveDate;
    
    @Column(name = "default_withdrawal_destination", length = 20)
    private String defaultWithdrawalDestination = "wallet"; // wallet, xysave
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constants
    public static final int TIER_1_THRESHOLD = 10000;  // First 10,000 at 20% p.a
    public static final int TIER_2_THRESHOLD = 100000; // 10,001 - 100,000 at 16% p.a
    public static final double TIER_3_RATE = 0.08;     // Above 100,000 at 8% p.a
    public static final double TIER_1_RATE = 0.20;     // 20% p.a for first 10,000
    public static final double TIER_2_RATE = 0.16;     // 16% p.a for 10,001 - 100,000
    
    // Constructors
    public SpendAndSaveAccount() {}
    
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
    
    public BigDecimal getTotalSavedFromSpending() {
        return totalSavedFromSpending;
    }
    
    public void setTotalSavedFromSpending(BigDecimal totalSavedFromSpending) {
        this.totalSavedFromSpending = totalSavedFromSpending;
    }
    
    public Boolean getActive() {
        return isActive;
    }
    
    public void setActive(Boolean active) {
        isActive = active;
    }
    
    public BigDecimal getSavingsPercentage() {
        return savingsPercentage;
    }
    
    public void setSavingsPercentage(BigDecimal savingsPercentage) {
        this.savingsPercentage = savingsPercentage;
    }
    
    public BigDecimal getMinTransactionAmount() {
        return minTransactionAmount;
    }
    
    public void setMinTransactionAmount(BigDecimal minTransactionAmount) {
        this.minTransactionAmount = minTransactionAmount;
    }
    
    public BigDecimal getDailyTier1Rate() {
        return dailyTier1Rate;
    }
    
    public void setDailyTier1Rate(BigDecimal dailyTier1Rate) {
        this.dailyTier1Rate = dailyTier1Rate;
    }
    
    public BigDecimal getDailyTier2Rate() {
        return dailyTier2Rate;
    }
    
    public void setDailyTier2Rate(BigDecimal dailyTier2Rate) {
        this.dailyTier2Rate = dailyTier2Rate;
    }
    
    public BigDecimal getDailyTier3Rate() {
        return dailyTier3Rate;
    }
    
    public void setDailyTier3Rate(BigDecimal dailyTier3Rate) {
        this.dailyTier3Rate = dailyTier3Rate;
    }
    
    public LocalDateTime getLastInterestCalculation() {
        return lastInterestCalculation;
    }
    
    public void setLastInterestCalculation(LocalDateTime lastInterestCalculation) {
        this.lastInterestCalculation = lastInterestCalculation;
    }
    
    public Integer getTotalTransactionsProcessed() {
        return totalTransactionsProcessed;
    }
    
    public void setTotalTransactionsProcessed(Integer totalTransactionsProcessed) {
        this.totalTransactionsProcessed = totalTransactionsProcessed;
    }
    
    public LocalDate getLastAutoSaveDate() {
        return lastAutoSaveDate;
    }
    
    public void setLastAutoSaveDate(LocalDate lastAutoSaveDate) {
        this.lastAutoSaveDate = lastAutoSaveDate;
    }
    
    public String getDefaultWithdrawalDestination() {
        return defaultWithdrawalDestination;
    }
    
    public void setDefaultWithdrawalDestination(String defaultWithdrawalDestination) {
        this.defaultWithdrawalDestination = defaultWithdrawalDestination;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public boolean canWithdraw(BigDecimal amount) {
        return balance != null && amount != null && 
               balance.compareTo(amount) >= 0 && isActive;
    }
    
    public void activate(BigDecimal savingsPercentage) {
        this.isActive = true;
        this.savingsPercentage = savingsPercentage;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
}