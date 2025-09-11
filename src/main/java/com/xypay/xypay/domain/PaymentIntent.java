package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment_intents", indexes = {
    @Index(name = "idx_payment_intent_user_status", columnList = "user_id, status, created_at"),
    @Index(name = "idx_payment_intent_order_id", columnList = "order_id"),
    @Index(name = "idx_payment_intent_merchant_id", columnList = "merchant_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaymentIntent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_id", length = 128, nullable = false)
    private String orderId;

    @Column(name = "merchant_id", length = 128, nullable = false)
    private String merchantId;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "currency", length = 5, nullable = false)
    private String currency = "NGN";

    @Column(name = "status", length = 32, nullable = false)
    private String status = "requires_confirmation"; // requires_confirmation, processing, succeeded, failed, canceled

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "reference", length = 100, unique = true)
    private String reference;

    @Column(name = "escrowed", nullable = false)
    private Boolean escrowed = false;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata = "{}";

    // Constructors
    public PaymentIntent() {}

    public PaymentIntent(User user, String orderId, String merchantId, BigDecimal amount) {
        this.user = user;
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.amount = amount;
        generateReference();
    }

    // Business methods
    public void markAsProcessing() {
        this.status = "processing";
    }

    public void markAsSucceeded() {
        this.status = "succeeded";
    }

    public void markAsFailed() {
        this.status = "failed";
    }

    public void markAsCanceled() {
        this.status = "canceled";
    }

    public boolean isCompleted() {
        return "succeeded".equals(status) || "failed".equals(status) || "canceled".equals(status);
    }

    // Methods expected by PaymentIntentService
    public void fail(String reason) {
        this.status = "failed";
        // Could store reason in metadata if needed
        if (reason != null && !reason.trim().isEmpty()) {
            this.metadata = "{\"failure_reason\":\"" + reason.replace("\"", "\\\"") + "\"}";
        }
    }

    public void confirm() {
        this.status = "succeeded";
    }

    public void cancel(String reason) {
        this.status = "canceled";
        // Could store reason in metadata if needed
        if (reason != null && !reason.trim().isEmpty()) {
            this.metadata = "{\"cancellation_reason\":\"" + reason.replace("\"", "\\\"") + "\"}";
        }
    }

    private void generateReference() {
        if (this.reference == null) {
            String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            String timeStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
            String uniquePart = String.valueOf(System.currentTimeMillis()).substring(7);
            String suffix = user != null && user.getWallet() != null ? 
                user.getWallet().getAccountNumber().substring(Math.max(0, user.getWallet().getAccountNumber().length() - 4)) : "0000";
            this.reference = String.format("PI-%s-%s-%s-%s", dateStr, timeStr, suffix, uniquePart);
        }
    }

    @PrePersist
    protected void onCreate() {
        generateReference();
    }

    @Override
    public String toString() {
        return String.format("PaymentIntent{orderId='%s', merchantId='%s', amount=%s, status='%s'}", 
            orderId, merchantId, amount, status);
    }
}
