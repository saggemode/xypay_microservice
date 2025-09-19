package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "journal_entries")
public class JournalEntry extends BaseEntity {
    
    @Column(name = "tx_id")
    private UUID txId;
    
    @Column(name = "account_id")
    private UUID accountId;
    
    @Column(name = "debit_credit")
    private String debitCredit;
    
    private BigDecimal amount;
    
    private String currency;
    
    @Column(name = "gl_account")
    private String glAccount;
    
    

    public UUID getTxId() {
        return txId;
    }

    public void setTxId(UUID txId) {
        this.txId = txId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }
}