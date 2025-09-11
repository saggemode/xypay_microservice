package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bulk_transfer_items", indexes = {
    @Index(name = "idx_bulk_item_bulk_transfer", columnList = "bulk_transfer_id"),
    @Index(name = "idx_bulk_item_status", columnList = "status"),
    @Index(name = "idx_bulk_item_recipient", columnList = "recipient_account_number")
})
public class BulkTransferItem extends BaseEntity {
    
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulk_transfer_id", nullable = false)
    private BulkTransfer bulkTransfer;
    
    @Column(name = "recipient_account_number", length = 20, nullable = false)
    private String recipientAccountNumber;
    
    @Column(name = "recipient_name", length = 255)
    private String recipientName;
    
    @Column(name = "recipient_bank_code", length = 10)
    private String recipientBankCode;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "reference", length = 50)
    private String reference;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "transaction_id")
    private Long transactionId; // Reference to the created transaction
    
    @Column(name = "bank_transfer_id")
    private Long bankTransferId; // Reference to the created bank transfer
    
    @Column(name = "row_number")
    private Integer rowNumber; // Row number in the original file
    
    // Constructors
    public BulkTransferItem() {}
    
    public BulkTransferItem(BulkTransfer bulkTransfer, String recipientAccountNumber, 
                           String recipientName, BigDecimal amount, String description) {
        this.bulkTransfer = bulkTransfer;
        this.recipientAccountNumber = recipientAccountNumber;
        this.recipientName = recipientName;
        this.amount = amount;
        this.description = description;
    }
    
    // Business methods
    public void markAsProcessing() {
        this.status = Status.PROCESSING;
    }
    
    public void markAsCompleted(Long transactionId, Long bankTransferId) {
        this.status = Status.COMPLETED;
        this.transactionId = transactionId;
        this.bankTransferId = bankTransferId;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = Status.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = Status.CANCELLED;
        this.processedAt = LocalDateTime.now();
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
}
