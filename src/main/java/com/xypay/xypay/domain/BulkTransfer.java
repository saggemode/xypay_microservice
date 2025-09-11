package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bulk_transfers", indexes = {
    @Index(name = "idx_bulk_transfer_user", columnList = "user_id"),
    @Index(name = "idx_bulk_transfer_status", columnList = "status"),
    @Index(name = "idx_bulk_transfer_created", columnList = "created_at")
})
public class BulkTransfer extends BaseEntity {
    
    public enum Status {
        PENDING("pending"),
        PROCESSING("processing"),
        COMPLETED("completed"),
        PARTIALLY_COMPLETED("partially_completed"),
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "batch_id", unique = true, nullable = false)
    private String batchId;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "total_recipients")
    private Integer totalRecipients = 0;
    
    @Column(name = "successful_transfers")
    private Integer successfulTransfers = 0;
    
    @Column(name = "failed_transfers")
    private Integer failedTransfers = 0;
    
    @Column(name = "processed_amount", precision = 19, scale = 4)
    private BigDecimal processedAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;
    
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    
    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata
    
    @OneToMany(mappedBy = "bulkTransfer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BulkTransferItem> transferItems;
    
    // Constructors
    public BulkTransfer() {}
    
    public BulkTransfer(User user, String batchId, String description) {
        this.user = user;
        this.batchId = batchId;
        this.description = description;
    }
    
    // Business methods
    public void startProcessing() {
        this.status = Status.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
    }
    
    public void completeProcessing() {
        this.status = Status.COMPLETED;
        this.processingCompletedAt = LocalDateTime.now();
    }
    
    public void markAsPartiallyCompleted() {
        this.status = Status.PARTIALLY_COMPLETED;
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
    
    public boolean isCompleted() {
        return status == Status.COMPLETED || status == Status.PARTIALLY_COMPLETED;
    }
    
    public boolean isProcessing() {
        return status == Status.PROCESSING;
    }
    
    public BigDecimal getSuccessRate() {
        if (totalRecipients == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(successfulTransfers)
                .divide(BigDecimal.valueOf(totalRecipients), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
