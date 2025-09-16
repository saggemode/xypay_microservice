package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerEscalationDTO {
    private UUID id;
    
    @NotNull(message = "Customer is required")
    private UUID customerId;
    
    @NotNull(message = "Created by is required")
    private UUID createdById;
    
    private UUID assignedToId;
    
    @NotBlank(message = "Priority is required")
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    
    @NotBlank(message = "Status is required")
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String resolution;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolvedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Nested DTOs for related entities
    private CustomerSummaryDTO customer;
    private StaffProfileDTO createdBy;
    private StaffProfileDTO assignedTo;
    
    // Additional fields for enhanced functionality
    private String category; // COMPLAINT, INQUIRY, TECHNICAL, BILLING, etc.
    private String subcategory;
    private String source; // PHONE, EMAIL, CHAT, MOBILE_APP, WEB
    private String channel; // SUPPORT, SALES, TECHNICAL
    private String language;
    private String timezone;
    private String tags;
    private String attachments;
    private String relatedTransactions;
    private String relatedAccounts;
    private String slaDeadline;
    private String slaStatus; // ON_TIME, OVERDUE, AT_RISK
    private Integer responseTime; // in minutes
    private Integer resolutionTime; // in minutes
    private String satisfactionRating;
    private String feedback;
    private String followUpRequired;
    private String followUpDate;
    private String escalationReason;
    private String escalationLevel;
    private String escalationHistory;
    private String internalNotes;
    private String externalNotes;
    private String metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime assignedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActivityAt;
    
    // Helper class for customer summary
    @Data
    public static class CustomerSummaryDTO {
        private UUID id;
        private String username;
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String fullName;
        private String accountNumber;
        private String kycLevel;
        private Boolean isVerified;
        private String status;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime registeredAt;
    }
}
