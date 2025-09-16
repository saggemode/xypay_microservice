package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BankDTO {
    private UUID id;
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Bank code is required")
    @Size(max = 10, message = "Bank code must not exceed 10 characters")
    private String code;
    
    private String swiftCode;
    private String countryCode;
    private String slug;
    private String ussd;
    private String logo;
    private Boolean isActive = true;
    private String bankType; // COMMERCIAL, INVESTMENT, CENTRAL, ISLAMIC, COOPERATIVE, DEVELOPMENT
    private String licenseNumber;
    private String regulatoryAuthority;
    private String headOfficeAddress;
    private String contactEmail;
    private String contactPhone;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime establishedDate;
    
    private BigDecimal capitalAdequacyRatio;
    private BigDecimal tier1Capital;
    private BigDecimal totalAssets;
    
    // Fee and Levy Configuration
    private Boolean appliesToFees = true;
    private Boolean appliesToLevies = false;
    private BigDecimal minimumVatableAmount = BigDecimal.ZERO;
    
    // Exemption Settings
    private Boolean exemptInternalTransfers = false;
    private Boolean exemptInternationalTransfers = false;
    
    // Rounding Configuration
    private String roundingMethod = "none"; // none, nearest, up, down
    
    // Compliance
    private Boolean baselCompliant = false;
    private Boolean ifrsCompliant = false;
    private Boolean islamicBankingEnabled = false;
    
    // Additional fields for enhanced functionality
    private String website;
    private String mobileApp;
    private String customerService;
    private String supportEmail;
    private String supportPhone;
    private String workingHours;
    private String timezone;
    private String currency;
    private String language;
    private Boolean isOnlineBankingAvailable;
    private Boolean isMobileBankingAvailable;
    private Boolean isAtmNetworkAvailable;
    private Integer branchCount;
    private Integer atmCount;
    private String rating;
    private String description;
    private String metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
