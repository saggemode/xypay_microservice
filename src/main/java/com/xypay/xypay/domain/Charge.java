package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "charges", indexes = {
    @Index(name = "idx_charge_transaction", columnList = "transaction_id"),
    @Index(name = "idx_charge_status", columnList = "status")
})
public class Charge extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;
    
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "NGN";
    
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "receipt_url")
    private String receiptUrl;
    
    @Column(name = "waiver_reason", columnDefinition = "TEXT")
    private String waiverReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waived_by_id")
    private User waivedBy;
    
    // Constructors
    public Charge() {}
    
    public Charge(Transaction transaction, BigDecimal amount, String currency, String status) {
        this.transaction = transaction;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }
    
    // Business methods
    public void waive(User waivedBy, String reason) {
        this.waiverReason = reason;
        this.waivedBy = waivedBy;
        this.amount = BigDecimal.ZERO;
        this.status = "waived";
    }
}
