package com.xypay.xypay.dto;

import com.xypay.xypay.enums.TransferStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BankTransferDTO {
    private UUID id;
    private UUID userId;
    private String bankName;
    private String bankCode;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal vat;
    private BigDecimal levy;
    private String reference;
    private TransferStatus status;
    private String transferType;
    private String description;
    private String failureReason;
    private Boolean requiresApproval;
    private UUID approvedBy;
    private LocalDateTime approvedAt;
    private String nibssReference;
    private String idempotencyKey;
    private Boolean requires2fa;
    private Boolean twoFaVerified;
    private BigDecimal fraudScore;
    private Boolean isSuspicious;
    private String deviceFingerprint;
    private String ipAddress;
    private String userAgent;
    private String locationData;
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime lastRetryAt;
    private Boolean circuitBreakerTripped;
    private Boolean isScheduled;
    private LocalDateTime scheduledAt;
    private Boolean isRecurring;
    private String recurringPattern;
    private Boolean isBulk;
    private UUID bulkTransferId;
    private Integer bulkIndex;
    private Boolean isEscrow;
    private String escrowReleaseConditions;
    private LocalDateTime escrowExpiresAt;
    private LocalDateTime processingStartedAt;
    private LocalDateTime processingCompletedAt;
    private String externalServiceResponse;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
