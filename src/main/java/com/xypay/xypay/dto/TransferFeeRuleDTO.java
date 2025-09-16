package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransferFeeRuleDTO {
    private UUID id;
    
    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String name;
    
    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.00", message = "Minimum amount must be greater than or equal to 0")
    private BigDecimal minAmount;
    
    @NotNull(message = "Maximum amount is required")
    @DecimalMin(value = "0.00", message = "Maximum amount must be greater than or equal to 0")
    private BigDecimal maxAmount;
    
    @NotNull(message = "Fee percentage is required")
    @DecimalMin(value = "0.00", message = "Fee percentage must be greater than or equal to 0")
    private BigDecimal feePercent;
    
    @NotNull(message = "Fixed fee is required")
    @DecimalMin(value = "0.00", message = "Fixed fee must be greater than or equal to 0")
    private BigDecimal feeFixed;
    
    @NotBlank(message = "Bank type is required")
    private String bankType; // internal, external, both
    
    private String kycLevel; // tier_1, tier_2, tier_3
    
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
    
    @NotNull(message = "Priority is required")
    private Integer priority = 0;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Additional fields for enhanced functionality
    private String description;
    private String category; // STANDARD, PREMIUM, VIP, CORPORATE
    private String currency;
    private String transferType; // DOMESTIC, INTERNATIONAL, INTERNAL
    private String channel; // MOBILE, WEB, ATM, BRANCH
    private String customerSegment; // RETAIL, SME, CORPORATE, VIP
    private String accountType; // SAVINGS, CURRENT, FIXED_DEPOSIT
    private String feeStructure; // PERCENTAGE, FIXED, TIERED, HYBRID
    private String calculationMethod; // SIMPLE, COMPOUND, PROGRESSIVE
    private String roundingRule; // UP, DOWN, NEAREST, NONE
    private BigDecimal capAmount;
    private BigDecimal floorAmount;
    private String effectiveDate;
    private String expiryDate;
    private String approvalStatus; // DRAFT, PENDING, APPROVED, REJECTED
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String createdBy;
    private String lastModifiedBy;
    private String version;
    private String status; // ACTIVE, INACTIVE, SUSPENDED, EXPIRED
    private String notes;
    private String metadata;
    private String tags;
    private String applicableRegions;
    private String applicableBanks;
    private String applicableCustomers;
    private String exceptions;
    private String overrides;
    private String auditTrail;
    private String complianceFlags;
    private String riskFlags;
    private String performanceMetrics;
    private String usageStatistics;
    
    // Helper methods
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(minAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(maxAmount) > 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal percentageFee = amount.multiply(feePercent);
        return percentageFee.add(feeFixed);
    }
    
    public boolean isApplicable(BigDecimal amount, String transferType, String kycLevel) {
        if (!isActive) {
            return false;
        }
        
        if (amount.compareTo(minAmount) < 0) {
            return false;
        }
        
        if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        
        if (!bankType.equals("both") && !bankType.equals(transferType)) {
            return false;
        }
        
        if (this.kycLevel != null && kycLevel != null && !kycLevel.equals(this.kycLevel)) {
            return false;
        }
        
        return true;
    }
}