package com.xypay.treasury.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "treasury_transactions")
public class TreasuryTransaction extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treasury_position_id")
    private TreasuryPosition treasuryPosition;
    
    @Column(name = "transaction_type", length = 20, nullable = false)
    private String transactionType; // DEBIT, CREDIT, TRANSFER, ADJUSTMENT
    
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;
    
    @Column(name = "reference", length = 50, unique = true)
    private String reference;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING"; // PENDING, COMPLETED, FAILED, CANCELLED
    
    @Column(name = "value_date", nullable = false)
    private LocalDateTime valueDate;
    
    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;
    
    @Column(name = "counterparty", length = 100)
    private String counterparty;
    
    @Column(name = "external_reference", length = 100)
    private String externalReference;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_category")
    private TransactionCategory transactionCategory;
    
    public enum TransactionCategory {
        LIQUIDITY_MANAGEMENT, INVESTMENT, BORROWING, LENDING, HEDGING, ADJUSTMENT
    }
}
