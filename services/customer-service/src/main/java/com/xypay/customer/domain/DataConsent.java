package com.xypay.customer.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_consents")
public class DataConsent {
    
    public enum ConsentType {
        MARKETING_EMAILS("marketing_emails", "Marketing Emails"),
        MARKETING_SMS("marketing_sms", "Marketing SMS"),
        MARKETING_PUSH("marketing_push", "Marketing Push Notifications"),
        DATA_PROCESSING("data_processing", "Data Processing"),
        DATA_SHARING("data_sharing", "Data Sharing with Third Parties"),
        ANALYTICS("analytics", "Analytics and Tracking"),
        COOKIES("cookies", "Cookie Usage"),
        LOCATION("location", "Location Data"),
        BIOMETRIC("biometric", "Biometric Data"),
        THIRD_PARTY("third_party", "Third Party Services");
        
        private final String value;
        private final String displayName;
        
        ConsentType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum ConsentStatus {
        GRANTED("granted", "Granted"),
        DENIED("denied", "Denied"),
        WITHDRAWN("withdrawn", "Withdrawn"),
        EXPIRED("expired", "Expired");
        
        private final String value;
        private final String displayName;
        
        ConsentStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false)
    private ConsentType consentType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsentStatus status;
    
    @Column(name = "consent_given_at")
    private LocalDateTime consentGivenAt;
    
    @Column(name = "consent_withdrawn_at")
    private LocalDateTime consentWithdrawnAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "consent_version")
    private String consentVersion;
    
    @Column(name = "legal_basis")
    private String legalBasis;
    
    @Column(name = "purpose")
    private String purpose;
    
    @Column(name = "data_categories")
    private String dataCategories;
    
    @Column(name = "retention_period")
    private String retentionPeriod;
    
    @Column(name = "third_parties")
    private String thirdParties;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public DataConsent() {}
    
    public DataConsent(User user, ConsentType consentType, ConsentStatus status) {
        this.user = user;
        this.consentType = consentType;
        this.status = status;
        this.consentGivenAt = LocalDateTime.now();
    }
    
    // Business Logic Methods
    public void grantConsent(String ipAddress, String userAgent, String consentVersion) {
        this.status = ConsentStatus.GRANTED;
        this.consentGivenAt = LocalDateTime.now();
        this.consentWithdrawnAt = null;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.consentVersion = consentVersion;
    }
    
    public void withdrawConsent() {
        this.status = ConsentStatus.WITHDRAWN;
        this.consentWithdrawnAt = LocalDateTime.now();
    }
    
    public void denyConsent() {
        this.status = ConsentStatus.DENIED;
        this.consentGivenAt = null;
        this.consentWithdrawnAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return status == ConsentStatus.GRANTED && 
               (expiresAt == null || LocalDateTime.now().isBefore(expiresAt));
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public ConsentType getConsentType() { return consentType; }
    public void setConsentType(ConsentType consentType) { this.consentType = consentType; }
    
    public ConsentStatus getStatus() { return status; }
    public void setStatus(ConsentStatus status) { this.status = status; }
    
    public LocalDateTime getConsentGivenAt() { return consentGivenAt; }
    public void setConsentGivenAt(LocalDateTime consentGivenAt) { this.consentGivenAt = consentGivenAt; }
    
    public LocalDateTime getConsentWithdrawnAt() { return consentWithdrawnAt; }
    public void setConsentWithdrawnAt(LocalDateTime consentWithdrawnAt) { this.consentWithdrawnAt = consentWithdrawnAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getConsentVersion() { return consentVersion; }
    public void setConsentVersion(String consentVersion) { this.consentVersion = consentVersion; }
    
    public String getLegalBasis() { return legalBasis; }
    public void setLegalBasis(String legalBasis) { this.legalBasis = legalBasis; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public String getDataCategories() { return dataCategories; }
    public void setDataCategories(String dataCategories) { this.dataCategories = dataCategories; }
    
    public String getRetentionPeriod() { return retentionPeriod; }
    public void setRetentionPeriod(String retentionPeriod) { this.retentionPeriod = retentionPeriod; }
    
    public String getThirdParties() { return thirdParties; }
    public void setThirdParties(String thirdParties) { this.thirdParties = thirdParties; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
