package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payout_requests", indexes = {
    @Index(name = "idx_payout_merchant_status", columnList = "merchant_id, status, created_at")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PayoutRequest extends BaseEntity {

    @Column(name = "merchant_id", length = 128, nullable = false)
    private String merchantId;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "destination_bank_code", length = 10, nullable = false)
    private String destinationBankCode;

    @Column(name = "destination_account_number", length = 20, nullable = false)
    private String destinationAccountNumber;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "pending"; // pending, processing, succeeded, failed

    @Column(name = "reference", length = 100, unique = true)
    private String reference;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata = "{}";

    // Constructors
    public PayoutRequest() {}

    public PayoutRequest(String merchantId, BigDecimal amount, String destinationBankCode, String destinationAccountNumber) {
        this.merchantId = merchantId;
        this.amount = amount;
        this.destinationBankCode = destinationBankCode;
        this.destinationAccountNumber = destinationAccountNumber;
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

    public boolean isCompleted() {
        return "succeeded".equals(status) || "failed".equals(status);
    }

    private void generateReference() {
        if (this.reference == null) {
            this.reference = "PO-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        }
    }

    @PrePersist
    protected void onCreate() {
        generateReference();
    }

    @Override
    public String toString() {
        return String.format("PayoutRequest{merchantId='%s', amount=%s, status='%s'}", 
            merchantId, amount, status);
    }
}
