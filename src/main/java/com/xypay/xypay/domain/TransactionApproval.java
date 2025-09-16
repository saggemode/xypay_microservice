package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transaction_approvals", indexes = {
    @Index(name = "idx_approval_transaction", columnList = "transaction_id"),
    @Index(name = "idx_approval_status", columnList = "status"),
    @Index(name = "idx_approval_created", columnList = "created_at")
})
public class TransactionApproval extends BaseEntity {
    
    public enum ApprovalStatus {
        PENDING("pending", "Pending Approval"),
        APPROVED("approved", "Approved"),
        REJECTED("rejected", "Rejected"),
        ESCALATED("escalated", "Escalated");
        
        private final String code;
        private final String displayName;
        
        ApprovalStatus(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private StaffProfile requestedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private StaffProfile approvedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "escalation_reason", columnDefinition = "TEXT")
    private String escalationReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to_id")
    private StaffProfile escalatedTo;
    
    // Constructors
    public TransactionApproval() {}
    
    public TransactionApproval(Transaction transaction, StaffProfile requestedBy) {
        this.transaction = transaction;
        this.requestedBy = requestedBy;
    }
    
    // Business methods
    public void approve(StaffProfile approver, String reason) {
        this.status = ApprovalStatus.APPROVED;
        this.approvedBy = approver;
        this.reason = reason;
    }
    
    public void reject(StaffProfile approver, String reason) {
        this.status = ApprovalStatus.REJECTED;
        this.approvedBy = approver;
        this.reason = reason;
    }
    
    public void escalate(StaffProfile escalatedTo, String reason) {
        this.status = ApprovalStatus.ESCALATED;
        this.escalatedTo = escalatedTo;
        this.escalationReason = reason;
    }
}
