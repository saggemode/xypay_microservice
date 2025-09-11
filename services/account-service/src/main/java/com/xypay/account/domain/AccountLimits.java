package com.xypay.account.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_limits", indexes = {
    @Index(name = "idx_account_limits_account", columnList = "account_id"),
    @Index(name = "idx_account_limits_type", columnList = "limit_type")
})
public class AccountLimits {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_id", nullable = false)
    @NotNull
    private Long accountId;
    
    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;
    
    @Column(name = "limit_type", nullable = false, length = 50)
    private String limitType; // DAILY_DEBIT, DAILY_CREDIT, MONTHLY_DEBIT, MONTHLY_CREDIT, SINGLE_TRANSACTION, ATM_DAILY, TRANSFER_DAILY, etc.
    
    @Column(name = "limit_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal limitAmount;
    
    @Column(name = "used_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal usedAmount = BigDecimal.ZERO;
    
    @Column(name = "limit_period", length = 20)
    private String limitPeriod; // DAILY, WEEKLY, MONTHLY, YEARLY, SINGLE
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public AccountLimits() {
        this.createdAt = LocalDateTime.now();
        this.effectiveDate = LocalDateTime.now();
    }
    
    public AccountLimits(Long accountId, String accountNumber, String limitType, BigDecimal limitAmount, String limitPeriod) {
        this();
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.limitType = limitType;
        this.limitAmount = limitAmount;
        this.limitPeriod = limitPeriod;
    }
    
    // Business Logic Methods
    public boolean canTransact(BigDecimal amount) {
        if (!isActive) {
            return false;
        }
        
        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate)) {
            return false;
        }
        
        if (LocalDateTime.now().isBefore(effectiveDate)) {
            return false;
        }
        
        return usedAmount.add(amount).compareTo(limitAmount) <= 0;
    }
    
    public void useLimit(BigDecimal amount) {
        if (!canTransact(amount)) {
            throw new IllegalStateException("Transaction would exceed limit");
        }
        this.usedAmount = this.usedAmount.add(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void releaseLimit(BigDecimal amount) {
        this.usedAmount = this.usedAmount.subtract(amount);
        if (this.usedAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.usedAmount = BigDecimal.ZERO;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resetLimit() {
        this.usedAmount = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getRemainingLimit() {
        return limitAmount.subtract(usedAmount);
    }
    
    public boolean isLimitExceeded() {
        return usedAmount.compareTo(limitAmount) > 0;
    }
    
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getLimitType() { return limitType; }
    public void setLimitType(String limitType) { this.limitType = limitType; }
    
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
    
    public BigDecimal getUsedAmount() { return usedAmount; }
    public void setUsedAmount(BigDecimal usedAmount) { this.usedAmount = usedAmount; }
    
    public String getLimitPeriod() { return limitPeriod; }
    public void setLimitPeriod(String limitPeriod) { this.limitPeriod = limitPeriod; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
