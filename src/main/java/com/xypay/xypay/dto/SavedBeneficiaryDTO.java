package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SavedBeneficiaryDTO {
    private UUID id;
    
    @NotNull(message = "User is required")
    private UUID userId;
    
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Account number must be between 10 and 20 digits")
    private String accountNumber;
    
    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    private String accountName;
    
    @NotBlank(message = "Bank code is required")
    @Size(max = 10, message = "Bank code must not exceed 10 characters")
    private String bankCode;
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 255, message = "Bank name must not exceed 255 characters")
    private String bankName;
    
    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    private String nickname;
    
    @NotNull(message = "Verification status is required")
    private Boolean isVerified = false;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // User information (populated from related user)
    private String userEmail;
    private String userPhone;
    private String userName;
    
    // Additional fields for enhanced functionality
    private String verificationMethod; // BVN, ACCOUNT_LOOKUP, MANUAL
    private String verificationStatus; // PENDING, VERIFIED, FAILED, EXPIRED
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private String verificationReference;
    private String verificationNotes;
    private LocalDateTime lastUsed;
    private Integer usageCount = 0;
    private Boolean isFavorite = false;
    private String category; // FAMILY, FRIEND, BUSINESS, VENDOR, UTILITY
    private String relationship; // SPOUSE, PARENT, CHILD, SIBLING, FRIEND, COLLEAGUE, VENDOR
    private String description;
    private String notes;
    private String tags;
    private String metadata;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String complianceStatus; // CLEAR, FLAGGED, BLOCKED
    private String fraudScore;
    private String blacklistStatus; // CLEAR, WATCHLIST, BLACKLIST
    private String watchlistReason;
    private String blacklistReason;
    private LocalDateTime lastTransactionAt;
    private BigDecimal lastTransactionAmount;
    private String lastTransactionReference;
    private Integer totalTransactions = 0;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String averageTransactionAmount;
    private String frequency; // DAILY, WEEKLY, MONTHLY, OCCASIONAL
    private String pattern; // REGULAR, IRREGULAR, SEASONAL
    private String source; // MANUAL, IMPORT, API, MOBILE_APP, WEB
    private String channel; // MOBILE, WEB, ATM, BRANCH
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
    private String address;
    private String phone;
    private String email;
    private String website;
    private String socialMedia;
    private String businessType;
    private String industry;
    private String sector;
    private String size; // SMALL, MEDIUM, LARGE, ENTERPRISE
    private String revenue;
    private String employees;
    private String established;
    private String license;
    private String registration;
    private String taxId;
    private String vatNumber;
    private String creditRating;
    private String riskRating;
    private String complianceRating;
    private String performanceRating;
    private String satisfactionRating;
    private String feedback;
    private String complaints;
    private String disputes;
    private String chargebacks;
    private String refunds;
    private String cancellations;
    private String modifications;
    private String escalations;
    private String approvals;
    private String rejections;
    private String suspensions;
    private String terminations;
    private String reactivations;
    private String migrations;
    private String upgrades;
    private String downgrades;
    private String customizations;
    private String integrations;
    private String automations;
    private String optimizations;
    private String improvements;
    private String enhancements;
    private String innovations;
    private String transformations;
    private String modernizations;
    private String digitizations;
    private String standardizations;
    private String harmonizations;
    private String alignments;
    private String synchronizations;
    private String coordinations;
    private String collaborations;
    private String communications;
    private String escalations2;
    private String approvals2;
    private String decisions;
    private String authorities;
    private String accountabilities;
    private String responsibilities;
    private String roles;
    private String individuals;
    private String teams;
    private String departments;
    private String organizations;
    private String portfolios;
    private String programs;
    private String projects;
    private String initiatives;
    private String plans;
    private String tactics;
    private String strategies;
    private String approaches;
    private String methodologies;
    private String frameworks;
    private String standards;
    private String guidelines;
    private String procedures;
    private String policies;
    private String processes;
    private String systems;
    private String customers;
    private String operations;
    private String finances;
    private String businesses;
    private String technologies;
    private String legals;
    private String regulations;
    private String markets;
    private String competitions;
    private String revenues;
    private String costs;
    private String usages;
    private String performances;
    private String compliances;
    private String risks;
    private String audits;
    private String overrides;
    private String exceptions;
    private String segments;
    private String banks;
    private String regions;
    private String tags2;
    private String metadatas;
    private String notes2;
    private String statuses;
    private String versions;
    private String modifications2;
    private String creations;
    private String approvals3;
    private String reviews;
    private String expiries;
    private String effectives;
    private String roundings;
    private String floors;
    private String caps;
    private String calculations;
    private String structures;
    private String categories;
    private String complianceLevels;
    private String riskProfiles;
    private String volumes;
    private String relationships;
    private String accounts;
    private String frequencies;
    private String counts;
    private String days;
    private String times;
    private String channels;
    private String types;
    private String currencies;
    private String descriptions;
    private String names;
    private String priorities;
    private String actives;
    private String levels;
    private String bankTypes;
    private String fixeds;
    private String percents;
    private String maxs;
    private String mins;
    private String rules;
    private String fees;
    private String transfers;
    private String ids;
}