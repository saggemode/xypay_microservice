package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "security_transactions")
public class SecurityTransaction extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_id", nullable = false)
    private Security security;
    
    @Column(name = "transaction_number", length = 50, unique = true, nullable = false)
    private String transactionNumber;
    
    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    
    @Column(name = "quantity", precision = 19, scale = 6, nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "price", precision = 19, scale = 6, nullable = false)
    private BigDecimal price;
    
    @Column(name = "gross_amount", precision = 19, scale = 2)
    private BigDecimal grossAmount;
    
    @Column(name = "commission", precision = 19, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;
    
    @Column(name = "fees", precision = 19, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;
    
    @Column(name = "taxes", precision = 19, scale = 2)
    private BigDecimal taxes = BigDecimal.ZERO;
    
    @Column(name = "net_amount", precision = 19, scale = 2)
    private BigDecimal netAmount;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    @Column(name = "base_currency_amount", precision = 19, scale = 2)
    private BigDecimal baseCurrencyAmount;
    
    @Column(name = "trade_date")
    private LocalDateTime tradeDate;
    
    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;
    
    @Column(name = "value_date")
    private LocalDateTime valueDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "counterparty", length = 200)
    private String counterparty;
    
    @Column(name = "broker", length = 200)
    private String broker;
    
    @Column(name = "exchange", length = 50)
    private String exchange;
    
    @Column(name = "order_id", length = 50)
    private String orderId;
    
    @Column(name = "execution_id", length = 50)
    private String executionId;
    
    // Risk and Compliance
    @Column(name = "trader_id")
    private Long traderId;
    
    @Column(name = "authorized_by")
    private Long authorizedBy;
    
    @Column(name = "authorization_date")
    private LocalDateTime authorizationDate;
    
    @Column(name = "compliance_checked")
    private Boolean complianceChecked = false;
    
    @Column(name = "compliance_status")
    @Enumerated(EnumType.STRING)
    private ComplianceStatus complianceStatus = ComplianceStatus.PENDING;
    
    @Column(name = "limit_check_passed")
    private Boolean limitCheckPassed = false;
    
    // Settlement
    @Column(name = "settlement_status")
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;
    
    @Column(name = "settlement_reference", length = 100)
    private String settlementReference;
    
    @Column(name = "custodian_reference", length = 100)
    private String custodianReference;
    
    @Column(name = "failed_reason", length = 200)
    private String failedReason;
    
    // P&L
    @Column(name = "realized_pnl", precision = 19, scale = 2)
    private BigDecimal realizedPnl = BigDecimal.ZERO;
    
    @Column(name = "cost_basis", precision = 19, scale = 6)
    private BigDecimal costBasis;
    
    // Workflow
    @Column(name = "approval_workflow_id")
    private Long approvalWorkflowId;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    public enum TransactionType {
        BUY, SELL, DIVIDEND, INTEREST, COUPON, MATURITY, CALL, PUT, 
        SPLIT, MERGER, SPINOFF, RIGHTS, BONUS, TRANSFER_IN, TRANSFER_OUT
    }
    
    public enum OrderType {
        MARKET, LIMIT, STOP, STOP_LIMIT, FILL_OR_KILL, IMMEDIATE_OR_CANCEL, 
        GOOD_TILL_CANCELLED, GOOD_TILL_DATE, AT_THE_OPENING, AT_THE_CLOSE
    }
    
    public enum TransactionStatus {
        PENDING, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED, EXPIRED
    }
    
    public enum ComplianceStatus {
        PENDING, APPROVED, REJECTED, REQUIRES_REVIEW
    }
    
    public enum SettlementStatus {
        PENDING, SETTLED, FAILED, CANCELLED
    }
}
