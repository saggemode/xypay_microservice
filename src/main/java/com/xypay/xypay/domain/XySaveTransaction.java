package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "xy_save_transactions")
public class XySaveTransaction extends BaseEntity {
    
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, INTEREST_CREDIT, AUTO_SAVE, TRANSFER_IN, TRANSFER_OUT
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xysave_account_id")
    private XySaveAccount xySaveAccount;
    
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
    
    @Column(name = "metadata")
    private String metadata = "{}";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public XySaveTransaction() {}
    
    // Getters and Setters
    public XySaveAccount getXySaveAccount() {
        return xySaveAccount;
    }
    
    public void setXySaveAccount(XySaveAccount xySaveAccount) {
        this.xySaveAccount = xySaveAccount;
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