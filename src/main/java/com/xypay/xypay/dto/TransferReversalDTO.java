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
public class TransferReversalDTO {
    private UUID id;
    
    @NotNull(message = "Original transfer is required")
    private UUID originalTransferId;
    
    private UUID reversalTransferId;
    
    @NotBlank(message = "Reversal reason is required")
    private String reason; // USER_REQUEST, SYSTEM_ERROR, FRAUD_DETECTION, BANK_ERROR, DUPLICATE_TRANSFER
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Status is required")
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    
    @NotNull(message = "Initiated by is required")
    private UUID initiatedById;
    
    private UUID approvedById;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
    
    // Nested DTOs for related entities
    private BankTransferSummaryDTO originalTransfer;
    private BankTransferSummaryDTO reversalTransfer;
    private UserSummaryDTO initiatedBy;
    private UserSummaryDTO approvedBy;
    
    // Additional fields for enhanced functionality
    private String reversalType; // FULL, PARTIAL, ADJUSTMENT
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    private String approvalNotes;
    private String processingNotes;
    private String failureReason;
    private String externalReference;
    private String internalReference;
    private String reversalMethod; // AUTOMATIC, MANUAL, SCHEDULED
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String category; // CUSTOMER_REQUEST, SYSTEM_ERROR, FRAUD, COMPLIANCE
    private String source; // MOBILE_APP, WEB, API, ADMIN, SYSTEM
    private String channel; // MOBILE, WEB, ATM, BRANCH, CALL_CENTER
    private String deviceFingerprint;
    private String ipAddress;
    private String userAgent;
    private String location;
    private String timezone;
    private String language;
    private String currency;
    private String country;
    private String state;
    private String city;
    private String metadata;
    private String tags;
    private String notes;
    private String comments;
    private String auditTrail;
    private String complianceFlags;
    private String riskFlags;
    private String fraudScore;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String complianceLevel; // STANDARD, ENHANCED, STRICT
    private String regulatoryCategory; // RETAIL, WHOLESALE, INTERBANK
    private String businessImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String customerImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String systemImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String operationalImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String financialImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String legalImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String regulatoryImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String marketImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String competitiveImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String reputationImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String brandImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String stakeholderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String shareholderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String employeeImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String partnerImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String vendorImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String supplierImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String distributorImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String retailerImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String wholesalerImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String manufacturerImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String serviceProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String technologyProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String infrastructureProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String platformProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String cloudProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String dataProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String analyticsProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String securityProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String complianceProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String auditProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String legalProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String regulatoryProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String consultingProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String advisoryProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String trainingProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String educationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String researchProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String developmentProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String innovationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String transformationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String modernizationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String digitizationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String automationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String optimizationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String improvementProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String enhancementProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String upgradeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String migrationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String integrationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String implementationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String deploymentProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String maintenanceProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String supportProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String serviceProviderImpact2; // LOW, MEDIUM, HIGH, CRITICAL
    private String helpProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String assistanceProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String guidanceProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String directionProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String leadershipProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String managementProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String administrationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String governanceProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String oversightProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String supervisionProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String monitoringProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String controlProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String qualityProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String assuranceProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String testingProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String validationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String verificationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String certificationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String accreditationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String standardizationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String harmonizationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String alignmentProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String synchronizationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String coordinationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String collaborationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String communicationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String escalationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String approvalProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String decisionProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String authorityProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String accountabilityProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String responsibilityProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String roleProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String individualProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String teamProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String departmentProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String organizationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String portfolioProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String programProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String projectProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String initiativeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String planProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String tacticProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String strategyProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String approachProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String methodologyProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String frameworkProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String standardProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String guidelineProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String procedureProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String policyProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String processProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String systemProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String customerProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String operationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String financeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String businessProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String technologyProviderImpact2; // LOW, MEDIUM, HIGH, CRITICAL
    private String legalProviderImpact2; // LOW, MEDIUM, HIGH, CRITICAL
    private String regulatoryProviderImpact2; // LOW, MEDIUM, HIGH, CRITICAL
    private String marketProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String competitiveProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String revenueProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String costProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String usageProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String performanceProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String complianceProviderImpact2; // LOW, MEDIUM, HIGH, CRITICAL
    private String riskProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String auditProviderImpact2; // LOW, MEDIUM, HIGH, CRITICAL
    private String overrideProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String exceptionProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String segmentProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String bankProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String regionProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String tagProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String metadataProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String noteProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String statusProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String versionProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String modificationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String creationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String approvalProviderImpact2; // LOW, MEDIUM, HIGH, CRITICAL
    private String reviewProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String expiryProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String effectiveProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String roundingProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String floorProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String capProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String calculationProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String structureProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String categoryProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String complianceLevelProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String riskProfileProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String volumeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String relationshipProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String accountProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String frequencyProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String countProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String dayProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String timeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String channelProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String typeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String currencyProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String descriptionProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String nameProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String priorityProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String activeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String levelProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String bankTypeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String fixedProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String percentProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String maxProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String minProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String ruleProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String feeProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String transferProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    private String idProviderImpact; // LOW, MEDIUM, HIGH, CRITICAL
    
    // Helper classes for nested DTOs
    @Data
    public static class BankTransferSummaryDTO {
        private UUID id;
        private String reference;
        private BigDecimal amount;
        private String status;
        private String bankName;
        private String accountNumber;
        private String description;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }
    
    @Data
    public static class UserSummaryDTO {
        private UUID id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
    }
}