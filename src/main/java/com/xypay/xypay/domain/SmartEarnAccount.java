package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "smartearn_accounts")
public class SmartEarnAccount extends BaseEntity {
    
    // SmartEarn specific constants
    public static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("0.2105"); // 21.05% annual
    public static final BigDecimal DAILY_INTEREST_RATE = ANNUAL_INTEREST_RATE.divide(new BigDecimal("365"), 8, RoundingMode.HALF_UP);
    public static final BigDecimal MIN_PROCESSING_FEE = new BigDecimal("10.00");
    public static final BigDecimal MAX_PROCESSING_FEE = new BigDecimal("3000.00");
    public static final BigDecimal PROCESSING_FEE_RATE = new BigDecimal("0.01"); // 1%
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "account_number", length = 20, unique = true, nullable = false)
    private String accountNumber;
    
    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "total_interest_earned", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalInterestEarned = BigDecimal.ZERO;
    
    @Column(name = "last_interest_calculation", nullable = false)
    private LocalDateTime lastInterestCalculation;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        lastInterestCalculation = LocalDateTime.now();
    }
    
    /**
     * Calculate processing fee for a given amount
     * 1% processing fee (minimum 10, capped at 3000)
     */
    public BigDecimal calculateProcessingFee(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal fee = amount.multiply(PROCESSING_FEE_RATE);
        
        // Apply minimum fee
        if (fee.compareTo(MIN_PROCESSING_FEE) < 0) {
            fee = MIN_PROCESSING_FEE;
        }
        
        // Apply maximum fee
        if (fee.compareTo(MAX_PROCESSING_FEE) > 0) {
            fee = MAX_PROCESSING_FEE;
        }
        
        return fee.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate daily interest for current balance
     */
    public BigDecimal calculateDailyInterest() {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        return balance.multiply(DAILY_INTEREST_RATE).setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate interest for a specific number of days
     */
    public BigDecimal calculateInterestForDays(int days) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0 || days <= 0) {
            return BigDecimal.ZERO;
        }
        
        return balance.multiply(DAILY_INTEREST_RATE).multiply(new BigDecimal(days))
                .setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Check if transaction time is before 10:30 AM on workdays
     */
    public boolean isBeforeCutoffTime(LocalDateTime transactionTime) {
        if (transactionTime == null) {
            return false;
        }
        
        // Check if it's a weekday (Monday = 1, Sunday = 7)
        int dayOfWeek = transactionTime.getDayOfWeek().getValue();
        if (dayOfWeek > 5) { // Saturday or Sunday
            return false;
        }
        
        // Check if time is before 10:30 AM
        return transactionTime.getHour() < 10 || 
               (transactionTime.getHour() == 10 && transactionTime.getMinute() < 30);
    }
    
    /**
     * Get confirmation date based on transaction time
     * Before 10:30 AM on workdays = same day
     * After 10:30 AM or weekends = T+1
     */
    public LocalDateTime getConfirmationDate(LocalDateTime transactionTime) {
        if (transactionTime == null) {
            return LocalDateTime.now();
        }
        
        if (isBeforeCutoffTime(transactionTime)) {
            return transactionTime.toLocalDate().atStartOfDay();
        } else {
            // T+1 - next business day
            LocalDateTime nextDay = transactionTime.plusDays(1);
            // Skip weekends
            while (nextDay.getDayOfWeek().getValue() > 5) {
                nextDay = nextDay.plusDays(1);
            }
            return nextDay.toLocalDate().atStartOfDay();
        }
    }
}
