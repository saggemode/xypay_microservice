package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "escrow_accounts", indexes = {
    @Index(name = "idx_escrow_buyer", columnList = "buyer_id"),
    @Index(name = "idx_escrow_seller", columnList = "seller_id"),
    @Index(name = "idx_escrow_status", columnList = "status"),
    @Index(name = "idx_escrow_created", columnList = "created_at")
})
public class EscrowAccount extends BaseEntity {
    
    public enum Status {
        PENDING("pending"),
        FUNDED("funded"),
        RELEASED("released"),
        DISPUTED("disputed"),
        REFUNDED("refunded"),
        EXPIRED("expired"),
        CANCELLED("cancelled");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @Column(name = "escrow_id", unique = true, nullable = false)
    private String escrowId;
    
    @Column(name = "title", length = 255, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "escrow_fee", precision = 19, scale = 4)
    private BigDecimal escrowFee = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "funded_at")
    private LocalDateTime fundedAt;
    
    @Column(name = "released_at")
    private LocalDateTime releasedAt;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "dispute_reason", columnDefinition = "TEXT")
    private String disputeReason;
    
    @Column(name = "dispute_raised_by")
    private UUID disputeRaisedBy; // User ID who raised the dispute
    
    @Column(name = "dispute_raised_at")
    private LocalDateTime disputeRaisedAt;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata
    
    // Constructors
    public EscrowAccount() {}
    
    public EscrowAccount(User buyer, User seller, String escrowId, String title, 
                        String description, BigDecimal amount) {
        this.buyer = buyer;
        this.seller = seller;
        this.escrowId = escrowId;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.totalAmount = amount.add(escrowFee);
    }
    
    // Business methods
    public void fund() {
        this.status = Status.FUNDED;
        this.fundedAt = LocalDateTime.now();
    }
    
    public void release() {
        this.status = Status.RELEASED;
        this.releasedAt = LocalDateTime.now();
    }
    
    public void refund() {
        this.status = Status.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }
    
    public void dispute(UUID raisedBy, String reason) {
        this.status = Status.DISPUTED;
        this.disputeRaisedBy = raisedBy;
        this.disputeReason = reason;
        this.disputeRaisedAt = LocalDateTime.now();
    }
    
    public void resolveDispute(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
        // Status will be updated based on resolution (released or refunded)
    }
    
    public void expire() {
        this.status = Status.EXPIRED;
    }
    
    public void cancel() {
        this.status = Status.CANCELLED;
    }
    
    public boolean isFunded() {
        return status == Status.FUNDED;
    }
    
    public boolean isReleased() {
        return status == Status.RELEASED;
    }
    
    public boolean isRefunded() {
        return status == Status.REFUNDED;
    }
    
    public boolean isDisputed() {
        return status == Status.DISPUTED;
    }
    
    public boolean isExpired() {
        return status == Status.EXPIRED;
    }
    
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }
    
    public boolean isExpiredByDate() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
    
    public void setEscrowFee(BigDecimal fee) {
        this.escrowFee = fee;
        this.totalAmount = this.amount.add(fee);
    }
}
