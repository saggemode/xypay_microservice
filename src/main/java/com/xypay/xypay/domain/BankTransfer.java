package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bank_transfers", indexes = {
    @Index(name = "idx_user_status_created", columnList = "user_id, status, created_at"),
    @Index(name = "idx_account_bank", columnList = "account_number, bank_code"),
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key"),
    @Index(name = "idx_bulk_transfer_index", columnList = "bulk_transfer_id, bulk_index"),
    @Index(name = "idx_scheduled_at", columnList = "scheduled_at"),
    @Index(name = "idx_fraud_score", columnList = "fraud_score")
})
public class BankTransfer extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Column(name = "bank_code", length = 10)
    private String bankCode;
    
    @Column(name = "account_number", length = 10)
    private String accountNumber;
    
    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;
    
    @Column(name = "fee", precision = 19, scale = 4)
    private BigDecimal fee = BigDecimal.ZERO;
    
    @Column(name = "vat", precision = 19, scale = 4)
    private BigDecimal vat = BigDecimal.ZERO;
    
    @Column(name = "levy", precision = 19, scale = 4)
    private BigDecimal levy = BigDecimal.ZERO;
    
    @Column(name = "reference", unique = true)
    private String reference;
    
    @Column(name = "status", length = 20)
    private String status = "pending"; // pending, processing, successful, completed, failed
    
    @Column(name = "transfer_type", length = 10)
    private String transferType = "inter"; // intra, inter
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "nibss_reference", length = 64)
    private String nibssReference;
    
    // Enhanced Security & Fraud Detection Fields
    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;
    
    @Column(name = "requires_2fa")
    private Boolean requires2fa = false;
    
    @Column(name = "two_fa_verified")
    private Boolean twoFaVerified = false;
    
    @Column(name = "two_fa_code", length = 6)
    private String twoFaCode;
    
    @Column(name = "two_fa_expires_at")
    private LocalDateTime twoFaExpiresAt;
    
    // Fraud Detection Fields
    @Column(name = "fraud_score", precision = 5, scale = 2)
    private BigDecimal fraudScore = BigDecimal.ZERO;
    
    @Column(name = "fraud_flags")
    private String fraudFlags = "{}";
    
    @Column(name = "is_suspicious")
    private Boolean isSuspicious = false;
    
    @Column(name = "reviewed_by_fraud_team")
    private Boolean reviewedByFraudTeam = false;
    
    // Device & Location Tracking
    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "location_data")
    private String locationData = "{}";
    
    // Retry & Circuit Breaker Fields
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;
    
    @Column(name = "circuit_breaker_tripped")
    private Boolean circuitBreakerTripped = false;
    
    // Scheduled & Recurring Transfer Fields
    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;
    
    @Column(name = "recurring_pattern", length = 50)
    private String recurringPattern;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_transfer_id")
    private BankTransfer parentTransfer;
    
    // Bulk Transfer Fields
    @Column(name = "is_bulk")
    private Boolean isBulk = false;
    
    @Column(name = "bulk_transfer_id")
    private Long bulkTransferId;
    
    @Column(name = "bulk_index")
    private Integer bulkIndex;
    
    // Escrow Fields
    @Column(name = "is_escrow")
    private Boolean isEscrow = false;
    
    @Column(name = "escrow_release_conditions")
    private String escrowReleaseConditions = "{}";
    
    @Column(name = "escrow_expires_at")
    private LocalDateTime escrowExpiresAt;
    
    // Enhanced Status Tracking
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    
    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;
    
    @Column(name = "external_service_response")
    private String externalServiceResponse = "{}";
    
    // Metadata for extensibility
    @Column(name = "metadata")
    private String metadata = "{}";
    
    // Constructors
    public BankTransfer() {}
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getFee() {
        return fee;
    }
    
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
    
    public BigDecimal getVat() {
        return vat;
    }
    
    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }
    
    public BigDecimal getLevy() {
        return levy;
    }
    
    public void setLevy(BigDecimal levy) {
        this.levy = levy;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTransferType() {
        return transferType;
    }
    
    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public Boolean getRequiresApproval() {
        return requiresApproval;
    }
    
    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }
    
    public User getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public String getNibssReference() {
        return nibssReference;
    }
    
    public void setNibssReference(String nibssReference) {
        this.nibssReference = nibssReference;
    }
    
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
    
    public Boolean getRequires2fa() {
        return requires2fa;
    }
    
    public void setRequires2fa(Boolean requires2fa) {
        this.requires2fa = requires2fa;
    }
    
    public Boolean getTwoFaVerified() {
        return twoFaVerified;
    }
    
    public void setTwoFaVerified(Boolean twoFaVerified) {
        this.twoFaVerified = twoFaVerified;
    }
    
    public String getTwoFaCode() {
        return twoFaCode;
    }
    
    public void setTwoFaCode(String twoFaCode) {
        this.twoFaCode = twoFaCode;
    }
    
    public LocalDateTime getTwoFaExpiresAt() {
        return twoFaExpiresAt;
    }
    
    public void setTwoFaExpiresAt(LocalDateTime twoFaExpiresAt) {
        this.twoFaExpiresAt = twoFaExpiresAt;
    }
    
    public BigDecimal getFraudScore() {
        return fraudScore;
    }
    
    public void setFraudScore(BigDecimal fraudScore) {
        this.fraudScore = fraudScore;
    }
    
    public String getFraudFlags() {
        return fraudFlags;
    }
    
    public void setFraudFlags(String fraudFlags) {
        this.fraudFlags = fraudFlags;
    }
    
    public Boolean getSuspicious() {
        return isSuspicious;
    }
    
    public void setSuspicious(Boolean suspicious) {
        isSuspicious = suspicious;
    }
    
    public Boolean getReviewedByFraudTeam() {
        return reviewedByFraudTeam;
    }
    
    public void setReviewedByFraudTeam(Boolean reviewedByFraudTeam) {
        this.reviewedByFraudTeam = reviewedByFraudTeam;
    }
    
    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }
    
    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
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
    
    public String getLocationData() {
        return locationData;
    }
    
    public void setLocationData(String locationData) {
        this.locationData = locationData;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public Integer getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }
    
    public void setLastRetryAt(LocalDateTime lastRetryAt) {
        this.lastRetryAt = lastRetryAt;
    }
    
    public Boolean getCircuitBreakerTripped() {
        return circuitBreakerTripped;
    }
    
    public void setCircuitBreakerTripped(Boolean circuitBreakerTripped) {
        this.circuitBreakerTripped = circuitBreakerTripped;
    }
    
    public Boolean getScheduled() {
        return isScheduled;
    }
    
    public void setScheduled(Boolean scheduled) {
        isScheduled = scheduled;
    }
    
    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }
    
    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
    
    public Boolean getRecurring() {
        return isRecurring;
    }
    
    public void setRecurring(Boolean recurring) {
        isRecurring = recurring;
    }
    
    public String getRecurringPattern() {
        return recurringPattern;
    }
    
    public void setRecurringPattern(String recurringPattern) {
        this.recurringPattern = recurringPattern;
    }
    
    public BankTransfer getParentTransfer() {
        return parentTransfer;
    }
    
    public void setParentTransfer(BankTransfer parentTransfer) {
        this.parentTransfer = parentTransfer;
    }
    
    public Boolean getBulk() {
        return isBulk;
    }
    
    public void setBulk(Boolean bulk) {
        isBulk = bulk;
    }
    
    public Long getBulkTransferId() {
        return bulkTransferId;
    }
    
    public void setBulkTransferId(Long bulkTransferId) {
        this.bulkTransferId = bulkTransferId;
    }
    
    public Integer getBulkIndex() {
        return bulkIndex;
    }
    
    public void setBulkIndex(Integer bulkIndex) {
        this.bulkIndex = bulkIndex;
    }
    
    public Boolean getEscrow() {
        return isEscrow;
    }
    
    public void setEscrow(Boolean escrow) {
        isEscrow = escrow;
    }
    
    public String getEscrowReleaseConditions() {
        return escrowReleaseConditions;
    }
    
    public void setEscrowReleaseConditions(String escrowReleaseConditions) {
        this.escrowReleaseConditions = escrowReleaseConditions;
    }
    
    public LocalDateTime getEscrowExpiresAt() {
        return escrowExpiresAt;
    }
    
    public void setEscrowExpiresAt(LocalDateTime escrowExpiresAt) {
        this.escrowExpiresAt = escrowExpiresAt;
    }
    
    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }
    
    public void setProcessingStartedAt(LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }
    
    public LocalDateTime getProcessingCompletedAt() {
        return processingCompletedAt;
    }
    
    public void setProcessingCompletedAt(LocalDateTime processingCompletedAt) {
        this.processingCompletedAt = processingCompletedAt;
    }
    
    public String getExternalServiceResponse() {
        return externalServiceResponse;
    }
    
    public void setExternalServiceResponse(String externalServiceResponse) {
        this.externalServiceResponse = externalServiceResponse;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}