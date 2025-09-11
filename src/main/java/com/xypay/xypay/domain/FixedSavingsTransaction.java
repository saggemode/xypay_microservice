package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FixedSavingsTransaction Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "fixed_savings_transactions")
public class FixedSavingsTransaction extends BaseEntity {
    
    public enum TransactionType {
        INITIAL_DEPOSIT, MATURITY_PAYOUT, EARLY_WITHDRAWAL, INTEREST_CREDIT, AUTO_RENEWAL
    }
    
    public enum Source {
        WALLET, XYSAVE, BOTH
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixed_savings_account_id")
    private FixedSavingsAccount fixedSavingsAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20)
    private TransactionType transactionType;
    
    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;
    
    @Column(name = "balance_before", precision = 19, scale = 4)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 19, scale = 4)
    private BigDecimal balanceAfter;
    
    @Column(name = "reference", unique = true)
    private String reference;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "interest_earned", precision = 19, scale = 4)
    private BigDecimal interestEarned = BigDecimal.ZERO;
    
    @Column(name = "interest_rate_applied", precision = 5, scale = 2)
    private BigDecimal interestRateApplied;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_account", length = 20)
    private Source sourceAccount;
    
    @Column(name = "source_transaction_id")
    private Long sourceTransactionId;
    
    @Column(name = "metadata")
    private String metadata = "{}";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public FixedSavingsTransaction() {}
    
    // Getters and Setters
    public FixedSavingsAccount getFixedSavingsAccount() {
        return fixedSavingsAccount;
    }
    
    public void setFixedSavingsAccount(FixedSavingsAccount fixedSavingsAccount) {
        this.fixedSavingsAccount = fixedSavingsAccount;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }
    
    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }
    
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getInterestEarned() {
        return interestEarned;
    }
    
    public void setInterestEarned(BigDecimal interestEarned) {
        this.interestEarned = interestEarned;
    }
    
    public BigDecimal getInterestRateApplied() {
        return interestRateApplied;
    }
    
    public void setInterestRateApplied(BigDecimal interestRateApplied) {
        this.interestRateApplied = interestRateApplied;
    }
    
    public Source getSourceAccount() {
        return sourceAccount;
    }
    
    public void setSourceAccount(Source sourceAccount) {
        this.sourceAccount = sourceAccount;
    }
    
    public Long getSourceTransactionId() {
        return sourceTransactionId;
    }
    
    public void setSourceTransactionId(Long sourceTransactionId) {
        this.sourceTransactionId = sourceTransactionId;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}