package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "bank_transfer_reversal")
public class TransferReversal {
    
    public enum ReversalReason {
        USER_REQUEST("user_request", "User Request"),
        SYSTEM_ERROR("system_error", "System Error"),
        FRAUD_DETECTION("fraud_detection", "Fraud Detection"),
        BANK_ERROR("bank_error", "Bank Error"),
        DUPLICATE_TRANSFER("duplicate_transfer", "Duplicate Transfer");
        
        private final String code;
        private final String description;
        
        ReversalReason(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_transfer_id", nullable = false)
    private BankTransfer originalTransfer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_transfer_id")
    private BankTransfer reversalTransfer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 50, nullable = false)
    private ReversalReason reason;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransferStatus status = TransferStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by", nullable = false)
    private User initiatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}