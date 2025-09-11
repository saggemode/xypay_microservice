package com.xypay.xypay.config;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "risk_compliance_configurations")
public class RiskComplianceConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "configuration_name")
    private String configurationName;
    
    @Column(name = "config_type")
    private String configType; // KYC, AML, SANCTIONS, FRAUD
    
    @Column(name = "kyc_workflow")
    private String kycWorkflow; // JSON format of KYC workflow steps
    
    @Column(name = "aml_threshold_amount")
    private BigDecimal amlThresholdAmount;
    
    @Column(name = "aml_velocity_limit")
    private Integer amlVelocityLimit; // Number of transactions in time period
    
    @Column(name = "aml_time_period_minutes")
    private Integer amlTimePeriodMinutes;
    
    @Column(name = "sanction_list_url")
    private String sanctionListUrl;
    
    @Column(name = "fraud_detection_rules")
    private String fraudDetectionRules; // JSON format of fraud detection rules
    
    @Column(name = "blacklist_accounts")
    private String blacklistAccounts; // JSON array of blacklisted account numbers
    
    @Column(name = "watchlist_accounts")
    private String watchlistAccounts; // JSON array of watched account numbers
    
    @Column(name = "alert_recipients")
    private String alertRecipients; // JSON array of email addresses or user IDs
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Explicit getter and setter for isActive field
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}