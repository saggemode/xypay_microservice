package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "bank_transfer_failure")
public class TransferFailure {
    
    public enum ErrorCategory {
        VALIDATION("validation", "Validation Error"),
        BUSINESS_LOGIC("business_logic", "Business Logic Error"),
        TECHNICAL("technical", "Technical Error"),
        EXTERNAL_SERVICE("external_service", "External Service Error"),
        FRAUD_DETECTION("fraud_detection", "Fraud Detection"),
        SECURITY("security", "Security Error"),
        SYSTEM("system", "System Error");
        
        private final String code;
        private final String description;
        
        ErrorCategory(String code, String description) {
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
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private BankTransfer transfer;
    
    @Column(name = "error_code", length = 50, nullable = false)
    private String errorCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "error_category", length = 50, nullable = false)
    private ErrorCategory errorCategory;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT", nullable = false)
    private String failureReason;
    
    @Column(name = "technical_details", columnDefinition = "JSON")
    private String technicalDetails = "{}";
    
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;
    
    @Column(name = "transfer_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal transferAmount;
    
    @Column(name = "recipient_account", length = 20, nullable = false)
    private String recipientAccount;
    
    @Column(name = "recipient_bank_code", length = 10)
    private String recipientBankCode;
    
    @Column(name = "failed_at", nullable = false, updatable = false)
    private LocalDateTime failedAt;
    
    @Column(name = "processing_duration")
    private Double processingDuration;
    
    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (failedAt == null) {
            failedAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void markResolved(User resolvedBy, String notes) {
        this.isResolved = true;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = notes;
    }
    
    public void incrementRetry() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }
    
    public boolean canRetry() {
        return this.retryCount < this.maxRetries && !this.isResolved;
    }
    
    public String getFailureSummary() {
        return String.format(
            "{\"error_code\":\"%s\",\"error_category\":\"%s\",\"failure_reason\":\"%s\",\"transfer_amount\":%.2f,\"recipient_account\":\"%s\",\"failed_at\":\"%s\",\"is_resolved\":%s,\"retry_count\":%d}",
            errorCode,
            errorCategory.getCode(),
            failureReason,
            transferAmount,
            recipientAccount,
            failedAt.toString(),
            isResolved,
            retryCount
        );
    }
}