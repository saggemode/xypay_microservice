package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "trade_amendments")
public class TradeAmendment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_finance_id", nullable = false)
    private TradeFinance tradeFinance;
    
    @Column(name = "amendment_number")
    private Integer amendmentNumber;
    
    @Column(name = "amendment_type")
    @Enumerated(EnumType.STRING)
    private AmendmentType amendmentType;
    
    @Column(name = "requested_by")
    private Long requestedBy;
    
    @Column(name = "request_date")
    private LocalDateTime requestDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AmendmentStatus status = AmendmentStatus.PENDING;
    
    @Column(name = "old_value", length = 1000)
    private String oldValue;
    
    @Column(name = "new_value", length = 1000)
    private String newValue;
    
    @Column(name = "field_changed", length = 100)
    private String fieldChanged;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "swift_message_sent")
    private Boolean swiftMessageSent = false;
    
    @Column(name = "charges", precision = 19, scale = 2)
    private BigDecimal charges = BigDecimal.ZERO;
    
    public enum AmendmentType {
        AMOUNT_INCREASE, AMOUNT_DECREASE, EXPIRY_EXTENSION, SHIPMENT_DATE_CHANGE,
        BENEFICIARY_CHANGE, DOCUMENT_CHANGE, TERMS_CHANGE, OTHER
    }
    
    public enum AmendmentStatus {
        PENDING, APPROVED, REJECTED, PROCESSED
    }
}
