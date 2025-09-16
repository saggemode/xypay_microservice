package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StaffActivityDTO {
    private UUID id;
    
    @NotNull(message = "Staff is required")
    private UUID staffId;
    
    @NotBlank(message = "Activity type is required")
    private String activityType; // TRANSACTION_PROCESSED, KYC_APPROVED, ESCALATION_HANDLED, etc.
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private Long relatedObjectId;
    private String relatedObjectType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Nested DTOs for related entities
    private StaffProfileDTO staff;
    
    // Additional fields for enhanced functionality
    private String category; // TRANSACTION, KYC, ESCALATION, CUSTOMER, REPORT, STAFF
    private String subcategory;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String status; // COMPLETED, IN_PROGRESS, FAILED, CANCELLED
    private String result; // SUCCESS, FAILURE, PARTIAL
    private String notes;
    private String comments;
    private String metadata;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String deviceFingerprint;
    private String location;
    private String timezone;
    private Integer duration; // in seconds
    private String performanceMetrics;
    private String errorDetails;
    private String stackTrace;
    private String relatedEntities;
    private String tags;
    private String attachments;
    private String auditTrail;
    private String complianceFlags;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String approvalRequired;
    private String approvalStatus;
    private UUID approvedBy;
    private LocalDateTime approvedAt;
    private String escalationReason;
    private String escalationLevel;
    private String followUpRequired;
    private LocalDateTime followUpDate;
    private String customerImpact;
    private String businessImpact;
    private String systemImpact;
    private String resolution;
    private String lessonsLearned;
    private String improvementSuggestions;
}
