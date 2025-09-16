package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransferFailureDTO {
    private UUID id;
    
    @NotNull(message = "Transfer is required")
    private UUID transferId;
    
    @NotBlank(message = "Error code is required")
    private String errorCode;
    
    @NotBlank(message = "Error category is required")
    private String errorCategory; // VALIDATION, BUSINESS_LOGIC, TECHNICAL, EXTERNAL_SERVICE, FRAUD_DETECTION, SECURITY, SYSTEM
    
    @NotBlank(message = "Failure reason is required")
    private String failureReason;
    
    private String technicalDetails;
    private String stackTrace;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private String ipAddress;
    private String userAgent;
    private String deviceFingerprint;
    
    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal transferAmount;
    
    @NotBlank(message = "Recipient account is required")
    private String recipientAccount;
    
    private String recipientBankCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime failedAt;
    
    private Double processingDuration;
    
    @NotNull(message = "Resolved status is required")
    private Boolean isResolved = false;
    
    private String resolutionNotes;
    
    private UUID resolvedById;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolvedAt;
    
    @NotNull(message = "Retry count is required")
    private Integer retryCount = 0;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastRetryAt;
    
    @NotNull(message = "Max retries is required")
    private Integer maxRetries = 3;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Additional fields for enhanced functionality
    private String transferReference;
    private String userEmail;
    private String failureSummary;
    private String transferContext;
    private String userContext;
    private String maskedEmail;
    private String category; // TECHNICAL, BUSINESS, SECURITY, COMPLIANCE
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private String status; // ACTIVE, RESOLVED, ESCALATED, CLOSED
    private String source; // SYSTEM, USER, EXTERNAL, AUTOMATED
    private String channel; // MOBILE, WEB, API, ATM, BRANCH
    private String environment; // PRODUCTION, STAGING, TEST, DEVELOPMENT
    private String version; // API_VERSION, SYSTEM_VERSION
    private String component; // FRONTEND, BACKEND, DATABASE, EXTERNAL_API
    private String module; // TRANSFER, PAYMENT, AUTH, KYC
    private String function; // CREATE_TRANSFER, PROCESS_TRANSFER, VALIDATE_TRANSFER
    private String operation; // READ, WRITE, UPDATE, DELETE
    private String resource; // ACCOUNT, TRANSFER, USER, WALLET
    private String action; // CREATE, UPDATE, DELETE, PROCESS
    private String result; // SUCCESS, FAILURE, PARTIAL, TIMEOUT
    private String outcome; // COMPLETED, FAILED, CANCELLED, PENDING
    private String impact; // NONE, LOW, MEDIUM, HIGH, CRITICAL
    private String severity; // INFO, WARNING, ERROR, CRITICAL
    private String urgency; // LOW, NORMAL, HIGH, URGENT
    private String classification; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality2; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity2; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability2; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity2; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality3; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity3; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability3; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity3; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality4; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity4; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability4; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity4; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality5; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity5; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability5; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity5; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality6; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity6; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability6; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity6; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality7; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity7; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability7; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity7; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality8; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity8; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability8; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity8; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality9; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity9; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability9; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity9; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String confidentiality10; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String integrity10; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String availability10; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private String sensitivity10; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    
    // Helper methods
    public String getFailureSummary() {
        return String.format(
            "{\"error_code\":\"%s\",\"error_category\":\"%s\",\"failure_reason\":\"%s\",\"transfer_amount\":%.2f,\"recipient_account\":\"%s\",\"failed_at\":\"%s\",\"is_resolved\":%s,\"retry_count\":%d}",
            errorCode,
            errorCategory,
            failureReason,
            transferAmount,
            recipientAccount,
            failedAt.toString(),
            isResolved,
            retryCount
        );
    }
    
    public String getMaskedEmail() {
        if (userEmail != null && userEmail.contains("@")) {
            String[] parts = userEmail.split("@");
            if (parts.length == 2) {
                String username = parts[0];
                String domain = parts[1];
                String maskedUsername = username.length() > 2 ? 
                    username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1) : 
                    username;
                return maskedUsername + "@" + domain;
            }
        }
        return userEmail;
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries && !isResolved;
    }
}
