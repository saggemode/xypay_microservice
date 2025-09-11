package com.xypay.xypay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FixedSavingsTransactionDTO {
    private Long id;
    private Long fixedSavingsAccountId;
    private String transactionType;
    private String transactionTypeDisplay;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String reference;
    private String description;
    private BigDecimal interestEarned;
    private BigDecimal interestRateApplied;
    private String sourceAccount;
    private String sourceAccountDisplay;
    private Long sourceTransactionId;
    private String metadata;
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFixedSavingsAccountId() {
        return fixedSavingsAccountId;
    }

    public void setFixedSavingsAccountId(Long fixedSavingsAccountId) {
        this.fixedSavingsAccountId = fixedSavingsAccountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionTypeDisplay() {
        return transactionTypeDisplay;
    }

    public void setTransactionTypeDisplay(String transactionTypeDisplay) {
        this.transactionTypeDisplay = transactionTypeDisplay;
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

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getSourceAccountDisplay() {
        return sourceAccountDisplay;
    }

    public void setSourceAccountDisplay(String sourceAccountDisplay) {
        this.sourceAccountDisplay = sourceAccountDisplay;
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