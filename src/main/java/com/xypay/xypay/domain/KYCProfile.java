package com.xypay.xypay.domain;

import jakarta.persistence.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "kyc_profiles")
public class KYCProfile {
    
    public enum KYCLevel {
        TIER_1("tier_1", "Tier 1"),
        TIER_2("tier_2", "Tier 2"),
        TIER_3("tier_3", "Tier 3");
        
        private final String value;
        private final String displayName;
        
        KYCLevel(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum Gender {
        MALE("male", "Male"),
        FEMALE("female", "Female");
        
        private final String value;
        private final String displayName;
        
        Gender(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum GovtIdType {
        NATIONAL_ID("national_id", "National ID Card"),
        VOTERS_CARD("voters_card", "Voter's Card"),
        PASSPORT("passport", "International Passport"),
        DRIVERS_LICENSE("drivers_license", "Driver's License");
        
        private final String value;
        private final String displayName;
        
        GovtIdType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "bvn", length = 11, unique = true)
    private String bvn;
    
    @Column(name = "nin", length = 11, unique = true)
    private String nin;
    
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    @Column(name = "state", length = 100)
    private String state;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;
    
    @Column(name = "lga", length = 100)
    private String lga;
    
    @Column(name = "area", length = 100)
    private String area;
    
    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;
    
    @Column(name = "telephone_number", length = 15)
    private String telephoneNumber;
    
    @Column(name = "passport_photo")
    private String passportPhoto;
    
    @Column(name = "selfie")
    private String selfie;
    
    @Column(name = "id_document")
    private String idDocument;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_level", nullable = false, length = 10)
    private KYCLevel kycLevel = KYCLevel.TIER_1;
    
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    // Tier upgrade tracking
    @Column(name = "last_upgrade_check")
    private LocalDateTime lastUpgradeCheck;
    
    @Column(name = "upgrade_eligibility_score", nullable = false)
    private Integer upgradeEligibilityScore = 0;
    
    @Column(name = "upgrade_requested", nullable = false)
    private Boolean upgradeRequested = false;
    
    @Column(name = "upgrade_request_date")
    private LocalDateTime upgradeRequestDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "govt_id_type", length = 20)
    private GovtIdType govtIdType;
    
    @Column(name = "govt_id_document")
    private String govtIdDocument;
    
    @Column(name = "proof_of_address")
    private String proofOfAddress;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public KYCProfile() {
        this.createdAt = LocalDateTime.now();
    }
    
    public KYCProfile(User user, LocalDate dateOfBirth, String address) {
        this();
        this.user = user;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }
    
    // Business Logic Methods
    public boolean canUpgradeToTier2() {
        if (this.kycLevel != KYCLevel.TIER_1) {
            return false;
        }
        
        if (!this.isApproved) {
            return false;
        }
        
        return this.bvn != null || this.nin != null;
    }
    
    public boolean canUpgradeToTier3() {
        if (this.kycLevel != KYCLevel.TIER_2) {
            return false;
        }
        
        if (!this.isApproved) {
            return false;
        }
        
        return this.bvn != null && this.nin != null && 
               this.govtIdDocument != null && this.proofOfAddress != null;
    }
    
    public Map<String, Object> getUpgradeRequirements(KYCLevel targetTier) {
        Map<String, Object> requirements = new HashMap<>();
        
        if (targetTier == KYCLevel.TIER_2) {
            requirements.put("current_tier", this.kycLevel.getDisplayName());
            requirements.put("target_tier", targetTier.getDisplayName());
            
            List<String> reqList = new ArrayList<>();
            reqList.add("Must be on Tier 1");
            reqList.add("KYC must be approved");
            reqList.add("BVN or NIN required");
            requirements.put("requirements", reqList);
            
            Map<String, Boolean> status = new HashMap<>();
            status.put("is_tier_1", this.kycLevel == KYCLevel.TIER_1);
            status.put("is_approved", this.isApproved);
            status.put("has_bvn_or_nin", this.bvn != null || this.nin != null);
            requirements.put("current_status", status);
            
        } else if (targetTier == KYCLevel.TIER_3) {
            requirements.put("current_tier", this.kycLevel.getDisplayName());
            requirements.put("target_tier", targetTier.getDisplayName());
            
            List<String> reqList = new ArrayList<>();
            reqList.add("Must be on Tier 2");
            reqList.add("KYC must be approved");
            reqList.add("Both BVN and NIN required");
            reqList.add("Government ID document required");
            reqList.add("Proof of address required");
            requirements.put("requirements", reqList);
            
            Map<String, Boolean> status = new HashMap<>();
            status.put("is_tier_2", this.kycLevel == KYCLevel.TIER_2);
            status.put("is_approved", this.isApproved);
            status.put("has_bvn", this.bvn != null);
            status.put("has_nin", this.nin != null);
            status.put("has_govt_id", this.govtIdDocument != null);
            status.put("has_proof_of_address", this.proofOfAddress != null);
            requirements.put("current_status", status);
        }
        
        return requirements;
    }
    
    public void upgradeToTier2() throws IllegalStateException {
        if (!canUpgradeToTier2()) {
            throw new IllegalStateException("Cannot upgrade to Tier 2. Requirements not met.");
        }
        
        this.kycLevel = KYCLevel.TIER_2;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void upgradeToTier3() throws IllegalStateException {
        if (!canUpgradeToTier3()) {
            throw new IllegalStateException("Cannot upgrade to Tier 3. Requirements not met.");
        }
        
        this.kycLevel = KYCLevel.TIER_3;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Map<String, Object> getTierLimits() {
        Map<String, Object> limits = new HashMap<>();
        
        switch (this.kycLevel) {
            case TIER_1:
                limits.put("daily_transaction_limit", 50000.0);
                limits.put("max_balance_limit", 300000.0);
                limits.put("description", "Basic tier with limited transactions");
                break;
            case TIER_2:
                limits.put("daily_transaction_limit", 200000.0);
                limits.put("max_balance_limit", 500000.0);
                limits.put("description", "Enhanced tier with moderate limits");
                break;
            case TIER_3:
                limits.put("daily_transaction_limit", 5000000.0);
                limits.put("max_balance_limit", null); // Unlimited
                limits.put("description", "Premium tier with high limits");
                break;
        }
        
        return limits;
    }
    
    public Double getDailyTransactionLimit() {
        Map<String, Object> limits = getTierLimits();
        return (Double) limits.get("daily_transaction_limit");
    }
    
    public Double getMaxBalanceLimit() {
        Map<String, Object> limits = getTierLimits();
        return (Double) limits.get("max_balance_limit");
    }
    
    public boolean canTransactAmount(Double amount) {
        Double dailyLimit = getDailyTransactionLimit();
        
        if (dailyLimit != null && amount > dailyLimit) {
            return false;
        }
        
        return this.isApproved;
    }
    
    public void approve(User approver) {
        this.isApproved = true;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reject(String reason) {
        this.isApproved = false;
        this.approvedBy = null;
        this.approvedAt = null;
        this.rejectionReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getBvn() {
        return bvn;
    }
    
    public void setBvn(String bvn) {
        this.bvn = bvn;
    }
    
    public String getNin() {
        return nin;
    }
    
    public void setNin(String nin) {
        this.nin = nin;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getLga() {
        return lga;
    }
    
    public void setLga(String lga) {
        this.lga = lga;
    }
    
    public String getArea() {
        return area;
    }
    
    public void setArea(String area) {
        this.area = area;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getTelephoneNumber() {
        return telephoneNumber;
    }
    
    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }
    
    public String getPassportPhoto() {
        return passportPhoto;
    }
    
    public void setPassportPhoto(String passportPhoto) {
        this.passportPhoto = passportPhoto;
    }
    
    public String getSelfie() {
        return selfie;
    }
    
    public void setSelfie(String selfie) {
        this.selfie = selfie;
    }
    
    public String getIdDocument() {
        return idDocument;
    }
    
    public void setIdDocument(String idDocument) {
        this.idDocument = idDocument;
    }
    
    public KYCLevel getKycLevel() {
        return kycLevel;
    }
    
    public void setKycLevel(KYCLevel kycLevel) {
        this.kycLevel = kycLevel;
    }
    
    public Boolean getIsApproved() {
        return isApproved;
    }
    
    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }
    
    public User getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public LocalDateTime getLastUpgradeCheck() {
        return lastUpgradeCheck;
    }
    
    public void setLastUpgradeCheck(LocalDateTime lastUpgradeCheck) {
        this.lastUpgradeCheck = lastUpgradeCheck;
    }
    
    public Integer getUpgradeEligibilityScore() {
        return upgradeEligibilityScore;
    }
    
    public void setUpgradeEligibilityScore(Integer upgradeEligibilityScore) {
        this.upgradeEligibilityScore = upgradeEligibilityScore;
    }
    
    public Boolean getUpgradeRequested() {
        return upgradeRequested;
    }
    
    public void setUpgradeRequested(Boolean upgradeRequested) {
        this.upgradeRequested = upgradeRequested;
    }
    
    public LocalDateTime getUpgradeRequestDate() {
        return upgradeRequestDate;
    }
    
    public void setUpgradeRequestDate(LocalDateTime upgradeRequestDate) {
        this.upgradeRequestDate = upgradeRequestDate;
    }
    
    public GovtIdType getGovtIdType() {
        return govtIdType;
    }
    
    public void setGovtIdType(GovtIdType govtIdType) {
        this.govtIdType = govtIdType;
    }
    
    public String getGovtIdDocument() {
        return govtIdDocument;
    }
    
    public void setGovtIdDocument(String govtIdDocument) {
        this.govtIdDocument = govtIdDocument;
    }
    
    public String getProofOfAddress() {
        return proofOfAddress;
    }
    
    public void setProofOfAddress(String proofOfAddress) {
        this.proofOfAddress = proofOfAddress;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}