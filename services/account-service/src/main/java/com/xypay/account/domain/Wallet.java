package com.xypay.account.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_user", columnList = "user_id"),
    @Index(name = "idx_wallet_account_number", columnList = "account_number"),
    @Index(name = "idx_wallet_alt_account_number", columnList = "alternative_account_number"),
    @Index(name = "idx_wallet_phone_alias", columnList = "phone_alias", unique = true)
})
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    @NotNull
    private Long userId;
    
    @Column(name = "branch_id")
    private Long branchId;
    
    @Column(name = "account_number", unique = true, nullable = false, length = 10)
    private String accountNumber;
    
    @Column(name = "alternative_account_number", unique = true, nullable = false, length = 10)
    private String alternativeAccountNumber;
    
    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "currency", length = 5, nullable = false)
    private String currency = "NGN";
    
    @Column(name = "phone_alias", unique = true, length = 15)
    private String phoneAlias;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Wallet() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Wallet(Long userId, String accountNumber) {
        this();
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.alternativeAccountNumber = generateAlternativeAccountNumber();
    }
    
    private String generateAlternativeAccountNumber() {
        // Generate alternative account number (different from main account number)
        return "ALT" + accountNumber;
    }
    
    // Business Logic Methods
    public boolean canDebit(BigDecimal amount) {
        return isActive && balance.compareTo(amount) >= 0;
    }
    
    public void debit(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalStateException("Insufficient balance or wallet inactive");
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void credit(BigDecimal amount) {
        if (!isActive) {
            throw new IllegalStateException("Wallet is inactive");
        }
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void freeze() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void unfreeze() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getAlternativeAccountNumber() { return alternativeAccountNumber; }
    public void setAlternativeAccountNumber(String alternativeAccountNumber) { this.alternativeAccountNumber = alternativeAccountNumber; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPhoneAlias() { return phoneAlias; }
    public void setPhoneAlias(String phoneAlias) { this.phoneAlias = phoneAlias; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
