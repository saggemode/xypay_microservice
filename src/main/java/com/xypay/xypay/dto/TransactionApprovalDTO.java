package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionApprovalDTO {
    private UUID id;
    
    @NotNull(message = "Transaction is required")
    private UUID transactionId;
    
    @NotNull(message = "Requested by is required")
    private UUID requestedById;
    
    private UUID approvedById;
    private UUID escalatedToId;
    
    private String status; // PENDING, APPROVED, REJECTED, ESCALATED
    private String reason;
    private String escalationReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Nested DTOs for related entities
    private TransactionSummaryDTO transaction;
    private StaffProfileDTO requestedBy;
    private StaffProfileDTO approvedBy;
    private StaffProfileDTO escalatedTo;
    
    // Additional fields for enhanced functionality
    private String approvalType; // MANUAL, AUTOMATIC, ESCALATED
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String category; // TRANSFER, WITHDRAWAL, PAYMENT, etc.
    private BigDecimal amount;
    private String currency;
    private String description;
    private String notes;
    private String comments;
    private Boolean requiresAdditionalVerification;
    private String verificationMethod;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String fraudScore;
    private String complianceCheck;
    private String regulatoryApproval;
    private String externalReference;
    private String metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime escalatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime rejectedAt;
    
    // Helper class for transaction summary
    @Data
    public static class TransactionSummaryDTO {
        private UUID id;
        private String reference;
        private BigDecimal amount;
        private String type;
        private String status;
        private String description;
        private String currency;
        private String channel;
        private String direction;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timestamp;
    }
}
