package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bank_transfer_failure")
public class TransferFailure extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private BankTransfer transfer;
    
    @Column(name = "error_code", length = 50)
    private String errorCode;
    
    @Column(name = "error_category", length = 50)
    private String errorCategory;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "technical_details")
    private String technicalDetails = "{}";
    
    @Column(name = "stack_trace")
    private String stackTrace;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;
    
    @Column(name = "recipient_account", length = 20)
    private String recipientAccount;
    
    @Column(name = "recipient_bank_code", length = 10)
    private String recipientBankCode;
    
    @Column(name = "transfer_amount", precision = 19, scale = 4)
    private BigDecimal transferAmount;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "processing_duration")
    private Double processingDuration;
    
    @Column(name = "is_resolved")
    private Boolean isResolved = false;
    
    @Column(name = "resolution_notes")
    private String resolutionNotes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    // Constructors
    public TransferFailure() {}
    
    // Getters and Setters
    public BankTransfer getTransfer() {
        return transfer;
    }
    
    public void setTransfer(BankTransfer transfer) {
        this.transfer = transfer;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorCategory() {
        return errorCategory;
    }
    
    public void setErrorCategory(String errorCategory) {
        this.errorCategory = errorCategory;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public String getTechnicalDetails() {
        return technicalDetails;
    }
    
    public void setTechnicalDetails(String technicalDetails) {
        this.technicalDetails = technicalDetails;
    }
    
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }
    
    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
    
    public String getRecipientAccount() {
        return recipientAccount;
    }
    
    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }
    
    public String getRecipientBankCode() {
        return recipientBankCode;
    }
    
    public void setRecipientBankCode(String recipientBankCode) {
        this.recipientBankCode = recipientBankCode;
    }
    
    public BigDecimal getTransferAmount() {
        return transferAmount;
    }
    
    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }
    
    public LocalDateTime getFailedAt() {
        return failedAt;
    }
    
    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
    
    public Double getProcessingDuration() {
        return processingDuration;
    }
    
    public void setProcessingDuration(Double processingDuration) {
        this.processingDuration = processingDuration;
    }
    
    public Boolean getResolved() {
        return isResolved;
    }
    
    public void setResolved(Boolean resolved) {
        isResolved = resolved;
    }
    
    public String getResolutionNotes() {
        return resolutionNotes;
    }
    
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
    
    public User getResolvedBy() {
        return resolvedBy;
    }
    
    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }
    
    public void setLastRetryAt(LocalDateTime lastRetryAt) {
        this.lastRetryAt = lastRetryAt;
    }
    
    public Integer getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}