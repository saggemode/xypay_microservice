package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "spend_and_save_transactions")
public class SpendAndSaveTransaction extends BaseEntity {
    
    public enum TransactionType {
        AUTO_SAVE, WITHDRAWAL, INTEREST_CREDIT, MANUAL_DEPOSIT, TRANSFER_IN, TRANSFER_OUT
    }
    
    public enum WithdrawalDestination {
        WALLET, XYSAVE
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spend_and_save_account_id")
    private SpendAndSaveAccount spendAndSaveAccount;
    
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
    
    @Column(name = "original_transaction_id")
    private Long originalTransactionId;
    
    @Column(name = "original_transaction_amount", precision = 19, scale = 4)
    private BigDecimal originalTransactionAmount;
    
    @Column(name = "savings_percentage_applied", precision = 5, scale = 2)
    private BigDecimal savingsPercentageApplied;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "withdrawal_destination", length = 20)
    private WithdrawalDestination withdrawalDestination;
    
    @Column(name = "destination_account", length = 50)
    private String destinationAccount;
    
    @Column(name = "interest_earned", precision = 19, scale = 4)
    private BigDecimal interestEarned = BigDecimal.ZERO;
    
    @Column(name = "interest_breakdown")
    private String interestBreakdown = "{}";
    
    @Column(name = "metadata")
    private String metadata = "{}";
    
    @Column(name = "status", length = 20)
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public SpendAndSaveTransaction() {}
    
    // Getters and Setters
    public SpendAndSaveAccount getSpendAndSaveAccount() {
        return spendAndSaveAccount;
    }
    
    public void setSpendAndSaveAccount(SpendAndSaveAccount spendAndSaveAccount) {
        this.spendAndSaveAccount = spendAndSaveAccount;
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
    
    public Long getOriginalTransactionId() {
        return originalTransactionId;
    }
    
    public void setOriginalTransactionId(Long originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }
    
    public BigDecimal getOriginalTransactionAmount() {
        return originalTransactionAmount;
    }
    
    public void setOriginalTransactionAmount(BigDecimal originalTransactionAmount) {
        this.originalTransactionAmount = originalTransactionAmount;
    }
    
    public BigDecimal getSavingsPercentageApplied() {
        return savingsPercentageApplied;
    }
    
    public void setSavingsPercentageApplied(BigDecimal savingsPercentageApplied) {
        this.savingsPercentageApplied = savingsPercentageApplied;
    }
    
    public WithdrawalDestination getWithdrawalDestination() {
        return withdrawalDestination;
    }
    
    public void setWithdrawalDestination(WithdrawalDestination withdrawalDestination) {
        this.withdrawalDestination = withdrawalDestination;
    }
    
    public String getDestinationAccount() {
        return destinationAccount;
    }
    
    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }
    
    public BigDecimal getInterestEarned() {
        return interestEarned;
    }
    
    public void setInterestEarned(BigDecimal interestEarned) {
        this.interestEarned = interestEarned;
    }
    
    public String getInterestBreakdown() {
        return interestBreakdown;
    }
    
    public void setInterestBreakdown(String interestBreakdown) {
        this.interestBreakdown = interestBreakdown;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}