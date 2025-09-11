package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transfer_reversals", indexes = {
    @Index(name = "idx_reversal_original_transaction", columnList = "original_transaction_id"),
    @Index(name = "idx_reversal_reversal_transaction", columnList = "reversal_transaction_id"),
    @Index(name = "idx_reversal_status", columnList = "status"),
    @Index(name = "idx_reversal_created", columnList = "created_at")
})
public class TransferReversal extends BaseEntity {
    
    public enum Status {
        PENDING("pending"),
        PROCESSING("processing"),
        COMPLETED("completed"),
        FAILED("failed"),
        CANCELLED("cancelled");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public enum ReversalType {
        CUSTOMER_REQUEST("customer_request"),
        BANK_INITIATED("bank_initiated"),
        SYSTEM_ERROR("system_error"),
        FRAUD_DETECTION("fraud_detection"),
        COMPLIANCE("compliance");
        
        private final String value;
        
        ReversalType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_transaction_id", nullable = false)
    private Transaction originalTransaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_transaction_id")
    private Transaction reversalTransaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by_id", nullable = false)
    private User initiatedBy;
    
    @Column(name = "reversal_id", unique = true, nullable = false)
    private String reversalId;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reversal_type", nullable = false)
    private ReversalType reversalType;
    
    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;
    
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    
    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "approval_required")
    private Boolean approvalRequired = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata
    
    // Constructors
    public TransferReversal() {}
    
    public TransferReversal(Transaction originalTransaction, User initiatedBy, 
                           BigDecimal amount, ReversalType reversalType, String reason) {
        this.originalTransaction = originalTransaction;
        this.initiatedBy = initiatedBy;
        this.amount = amount;
        this.reversalType = reversalType;
        this.reason = reason;
        this.reversalId = "REV-" + System.currentTimeMillis();
    }
    
    // Business methods
    public void startProcessing() {
        this.status = Status.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
    }
    
    public void completeProcessing(Transaction reversalTransaction) {
        this.status = Status.COMPLETED;
        this.reversalTransaction = reversalTransaction;
        this.processingCompletedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = Status.FAILED;
        this.failureReason = reason;
        this.processingCompletedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = Status.CANCELLED;
        this.processingCompletedAt = LocalDateTime.now();
    }
    
    public void approve(User approver, String notes) {
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.approvalNotes = notes;
    }
    
    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == Status.FAILED;
    }
    
    public boolean isProcessing() {
        return status == Status.PROCESSING;
    }
    
    public boolean isPending() {
        return status == Status.PENDING;
    }
    
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }
    
    public boolean requiresApproval() {
        return approvalRequired && approvedBy == null;
    }
    
    public boolean isApproved() {
        return approvedBy != null;
    }
}
