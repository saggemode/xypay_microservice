package com.xypay.transaction.domain;

import com.xypay.transaction.enums.TransactionChannel;
import com.xypay.transaction.enums.TransactionStatus;
import com.xypay.transaction.enums.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_number", nullable = false)
    private String accountNumber;
    
    @Column(name = "receiver_account_number")
    private String receiverAccountNumber;
    
    @Column(name = "reference", unique = true, nullable = false)
    private String reference;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private TransactionType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false)
    private TransactionChannel channel;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "balance_after", precision = 19, scale = 4)
    private BigDecimal balanceAfter;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "currency", length = 5, nullable = false)
    private String currency = "NGN";
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
    
    @Column(name = "direction", length = 10, nullable = false)
    private String direction; // CREDIT, DEBIT
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // Constructors
    public Transaction() {}
    
    public Transaction(String accountNumber, String reference, BigDecimal amount, TransactionType type, TransactionChannel channel) {
        this.accountNumber = accountNumber;
        this.reference = reference;
        this.amount = amount;
        this.type = type;
        this.channel = channel;
        this.direction = type.isDebit() ? "DEBIT" : "CREDIT";
        this.idempotencyKey = UUID.randomUUID().toString();
    }
    
    // Business Logic Methods
    public void markAsSuccess(BigDecimal balanceAfter) {
        this.status = TransactionStatus.SUCCESS;
        this.balanceAfter = balanceAfter;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed() {
        this.status = TransactionStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsPending() {
        this.status = TransactionStatus.PENDING;
        this.processedAt = null;
    }
    
    public void markAsProcessing() {
        this.status = TransactionStatus.PROCESSING;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsCancelled() {
        this.status = TransactionStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsReversed() {
        this.status = TransactionStatus.REVERSED;
        this.processedAt = LocalDateTime.now();
    }
    
    public boolean isSuccess() {
        return TransactionStatus.SUCCESS.equals(this.status);
    }
    
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }
    
    public boolean isPending() {
        return TransactionStatus.PENDING.equals(this.status);
    }
    
    public boolean isProcessing() {
        return TransactionStatus.PROCESSING.equals(this.status);
    }
    
    public boolean isFinal() {
        return this.status.isFinal();
    }
    
    public boolean canBeReversed() {
        return this.status.canBeReversed();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getReceiverAccountNumber() { return receiverAccountNumber; }
    public void setReceiverAccountNumber(String receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    
    public TransactionChannel getChannel() { return channel; }
    public void setChannel(TransactionChannel channel) { this.channel = channel; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
