package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "treasury_settlements")
public class TreasurySettlement extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treasury_operation_id", nullable = false)
    private TreasuryOperation treasuryOperation;
    
    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;
    
    @Column(name = "settlement_amount", precision = 19, scale = 2)
    private BigDecimal settlementAmount;
    
    @Column(name = "settlement_currency", length = 3)
    private String settlementCurrency;
    
    @Column(name = "settlement_status")
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;
    
    @Column(name = "settlement_reference", length = 100)
    private String settlementReference;
    
    @Column(name = "counterparty_account", length = 50)
    private String counterpartyAccount;
    
    @Column(name = "our_account", length = 50)
    private String ourAccount;
    
    @Column(name = "settlement_method")
    @Enumerated(EnumType.STRING)
    private SettlementMethod settlementMethod = SettlementMethod.WIRE_TRANSFER;
    
    @Column(name = "settlement_instructions", length = 500)
    private String settlementInstructions;
    
    @Column(name = "failed_reason", length = 200)
    private String failedReason;
    
    public enum SettlementStatus {
        PENDING, SETTLED, FAILED, CANCELLED
    }
    
    public enum SettlementMethod {
        WIRE_TRANSFER, ACH, SWIFT, RTGS, BOOK_TRANSFER
    }
}
