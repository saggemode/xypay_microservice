package com.xypay.xypay.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    private String currency;
    
    @Column(name = "account_type")
    private String accountType;
    
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "ledger_balance")
    private java.math.BigDecimal ledgerBalance;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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

    public java.math.BigDecimal getLedgerBalance() { return ledgerBalance; }
    public void setLedgerBalance(java.math.BigDecimal ledgerBalance) { this.ledgerBalance = ledgerBalance; }
}