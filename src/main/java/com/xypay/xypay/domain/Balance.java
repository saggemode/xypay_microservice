package com.xypay.xypay.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "balances")
public class Balance {
    
    @Id
    @Column(name = "account_id")
    private Long accountId;
    
    @Column(name = "ledger_balance")
    private BigDecimal ledgerBalance;
    
    @Column(name = "available_balance")
    private BigDecimal availableBalance;
    
    @Column(name = "reserved_balance")
    private BigDecimal reservedBalance;
    
    @Column(name = "as_of")
    private LocalDateTime asOf;

    // Getters and Setters
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getLedgerBalance() {
        return ledgerBalance;
    }

    public void setLedgerBalance(BigDecimal ledgerBalance) {
        this.ledgerBalance = ledgerBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getReservedBalance() {
        return reservedBalance;
    }

    public void setReservedBalance(BigDecimal reservedBalance) {
        this.reservedBalance = reservedBalance;
    }

    public LocalDateTime getAsOf() {
        return asOf;
    }

    public void setAsOf(LocalDateTime asOf) {
        this.asOf = asOf;
    }
}