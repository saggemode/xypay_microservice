package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Wallet receiver;
    
    @Column(name = "reference", unique = true)
    private String reference;
    
    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;
    
    @Column(name = "type", length = 10)
    private String type; // CREDIT, DEBIT
    
    @Column(name = "channel", length = 20)
    private String channel; // TRANSFER, DEPOSIT, BILL
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "status", length = 10)
    private String status; // SUCCESS, PENDING, FAILED
    
    @Column(name = "balance_after", precision = 19, scale = 4)
    private BigDecimal balanceAfter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Transaction parent;
    
    @Column(name = "currency", length = 5)
    private String currency = "NGN";
    
    @Column(name = "metadata")
    private String metadata;
    
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
    
    @Column(name = "direction", length = 10)
    private String direction; // CREDIT, DEBIT
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // Constructors
    public Transaction() {}
    
    public Transaction(Wallet wallet, String reference, BigDecimal amount, String type, String channel) {
        this.wallet = wallet;
        this.reference = reference;
        this.amount = amount;
        this.type = type;
        this.channel = channel;
    }
    
    @CreationTimestamp
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // Getters and Setters
    public Wallet getWallet() {
        return wallet;
    }
    
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
    
    public Wallet getReceiver() {
        return receiver;
    }
    
    public void setReceiver(Wallet receiver) {
        this.receiver = receiver;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public Transaction getParent() {
        return parent;
    }
    
    public void setParent(Transaction parent) {
        this.parent = parent;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}