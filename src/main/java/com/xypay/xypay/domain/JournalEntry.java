package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "journal_entries")
public class JournalEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tx_id")
    private Long txId;
    
    @Column(name = "account_id")
    private Long accountId;
    
    @Column(name = "debit_credit")
    private String debitCredit;
    
    private BigDecimal amount;
    
    private String currency;
    
    @Column(name = "gl_account")
    private String glAccount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getTxId() {
        return txId;
    }

    public void setTxId(Long txId) {
        this.txId = txId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}