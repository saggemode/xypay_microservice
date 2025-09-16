package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LargeTransactionShieldSettingsDTO {
    private UUID id;
    
    @NotNull(message = "User is required")
    private UUID userId;
    
    @NotNull(message = "Enabled status is required")
    private Boolean enabled = false;
    
    @DecimalMin(value = "0.00", message = "Per transaction limit must be greater than or equal to 0")
    private BigDecimal perTransactionLimit;
    
    @DecimalMin(value = "0.00", message = "Daily limit must be greater than or equal to 0")
    private BigDecimal dailyLimit;
    
    @DecimalMin(value = "0.00", message = "Monthly limit must be greater than or equal to 0")
    private BigDecimal monthlyLimit;
    
    private String faceTemplateHash;
    private String faceTemplateAlg = "sha256";
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime faceRegisteredAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Additional fields for enhanced functionality
    private String userEmail;
    private String userPhone;
    private String userName;
    private String status; // ACTIVE, INACTIVE, SUSPENDED, EXPIRED
    private String version; // VERSION_1, VERSION_2, VERSION_3
    private String category; // SECURITY, PRIVACY, COMPLIANCE, RISK
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private String source; // USER, ADMIN, SYSTEM, AUTOMATED
    private String channel; // MOBILE, WEB, API, ADMIN
    private String environment; // PRODUCTION, STAGING, TEST, DEVELOPMENT
    private String component; // FRONTEND, BACKEND, DATABASE, EXTERNAL_API
    private String module; // SECURITY, AUTH, KYC, TRANSFER
    private String function; // ENABLE_SHIELD, DISABLE_SHIELD, UPDATE_LIMITS
    private String operation; // CREATE, READ, UPDATE, DELETE
    private String resource; // USER, SETTINGS, SECURITY, AUTH
    private String action; // ENABLE, DISABLE, UPDATE, RESET
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
    private String metadata;
    private String tags;
    private String notes;
    private String comments;
    private String auditTrail;
    private String complianceFlags;
    private String riskFlags;
    private String securityFlags;
    private String privacyFlags;
    private String regulatoryFlags;
    private String legalFlags;
    private String businessFlags;
    private String technicalFlags;
    private String operationalFlags;
    private String financialFlags;
    private String customerFlags;
    private String systemFlags;
    private String processFlags;
    private String policyFlags;
    private String procedureFlags;
    private String guidelineFlags;
    private String standardFlags;
    private String frameworkFlags;
    private String methodologyFlags;
    private String approachFlags;
    private String strategyFlags;
    private String tacticFlags;
    private String planFlags;
    private String initiativeFlags;
    private String projectFlags;
    private String programFlags;
    private String portfolioFlags;
    private String organizationFlags;
    private String departmentFlags;
    private String teamFlags;
    private String individualFlags;
    private String roleFlags;
    private String responsibilityFlags;
    private String accountabilityFlags;
    private String authorityFlags;
    private String decisionFlags;
    private String approvalFlags;
    private String escalationFlags;
    private String communicationFlags;
    private String coordinationFlags;
    private String collaborationFlags;
    private String integrationFlags;
    private String alignmentFlags;
    private String synchronizationFlags;
    private String harmonizationFlags;
    private String standardizationFlags;
    private String optimizationFlags;
    private String improvementFlags;
    private String enhancementFlags;
    private String innovationFlags;
    private String transformationFlags;
    private String modernizationFlags;
    private String digitizationFlags;
    private String automationFlags;
    private String efficiencyFlags;
    private String effectivenessFlags;
    private String productivityFlags;
    private String performanceFlags;
    private String qualityFlags;
    private String reliabilityFlags;
    private String availabilityFlags;
    private String scalabilityFlags;
    private String flexibilityFlags;
    private String adaptabilityFlags;
    private String agilityFlags;
    private String responsivenessFlags;
    private String timelinessFlags;
    private String accuracyFlags;
    private String precisionFlags;
    private String consistencyFlags;
    private String uniformityFlags;
    private String normalizationFlags;
    private String harmonizationFlags2;
    private String alignmentFlags2;
    private String synchronizationFlags2;
    private String coordinationFlags2;
    private String integrationFlags2;
    private String collaborationFlags2;
    private String communicationFlags2;
    private String escalationFlags2;
    private String approvalFlags2;
    private String decisionFlags2;
    private String authorityFlags2;
    private String accountabilityFlags2;
    private String responsibilityFlags2;
    private String roleFlags2;
    private String individualFlags2;
    private String teamFlags2;
    private String departmentFlags2;
    private String organizationFlags2;
    private String portfolioFlags2;
    private String programFlags2;
    private String projectFlags2;
    private String initiativeFlags2;
    private String planFlags2;
    private String tacticFlags2;
    private String strategyFlags2;
    private String approachFlags2;
    private String methodologyFlags2;
    private String frameworkFlags2;
    private String standardFlags2;
    private String guidelineFlags2;
    private String procedureFlags2;
    private String policyFlags2;
    private String processFlags2;
    private String systemFlags2;
    private String customerFlags2;
    private String operationFlags2;
    private String financeFlags2;
    private String businessFlags2;
    private String technologyFlags2;
    private String legalFlags2;
    private String regulatoryFlags2;
    private String marketFlags2;
    private String competitiveFlags2;
    private String revenueFlags2;
    private String costFlags2;
    private String usageFlags2;
    private String performanceFlags2;
    private String complianceFlags2;
    private String riskFlags2;
    private String auditFlags2;
    private String overrideFlags2;
    private String exceptionFlags2;
    private String segmentFlags2;
    private String bankFlags2;
    private String regionFlags2;
    private String tagFlags2;
    private String metadataFlags2;
    private String noteFlags2;
    private String statusFlags2;
    private String versionFlags2;
    private String modificationFlags2;
    private String creationFlags2;
    private String approvalFlags3;
    private String reviewFlags2;
    private String expiryFlags2;
    private String effectiveFlags2;
    private String roundingFlags2;
    private String floorFlags2;
    private String capFlags2;
    private String calculationFlags2;
    private String structureFlags2;
    private String categoryFlags2;
    private String complianceLevelFlags2;
    private String riskProfileFlags2;
    private String volumeFlags2;
    private String relationshipFlags2;
    private String accountFlags2;
    private String frequencyFlags2;
    private String countFlags2;
    private String dayFlags2;
    private String timeFlags2;
    private String channelFlags2;
    private String typeFlags2;
    private String currencyFlags2;
    private String descriptionFlags2;
    private String nameFlags2;
    private String priorityFlags2;
    private String activeFlags2;
    private String levelFlags2;
    private String bankTypeFlags2;
    private String fixedFlags2;
    private String percentFlags2;
    private String maxFlags2;
    private String minFlags2;
    private String ruleFlags2;
    private String feeFlags2;
    private String transferFlags2;
    private String idFlags2;
    
    // Helper methods
    public boolean requiresVerification(BigDecimal amount) {
        if (!enabled) {
            return false;
        }
        
        return perTransactionLimit != null && amount.compareTo(perTransactionLimit) > 0;
    }
    
    public boolean hasFaceRegistered() {
        return faceTemplateHash != null && faceRegisteredAt != null;
    }
}
