package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_preferences")
public class UserPreferences extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Spend and Save Preferences
    @Column(name = "spend_save_enabled")
    private Boolean spendSaveEnabled = false;
    
    @Column(name = "savings_percentage", precision = 5, scale = 2)
    private BigDecimal savingsPercentage = new BigDecimal("5.00");
    
    @Column(name = "min_transaction_amount", precision = 19, scale = 4)
    private BigDecimal minTransactionAmount = new BigDecimal("100.00");
    
    @Column(name = "funding_source", length = 20)
    private String fundingSource = "wallet"; // wallet, xysave, both
    
    @Column(name = "auto_withdrawal_enabled")
    private Boolean autoWithdrawalEnabled = false;
    
    @Column(name = "auto_withdrawal_threshold", precision = 19, scale = 4)
    private BigDecimal autoWithdrawalThreshold = new BigDecimal("10000.00");
    
    @Column(name = "withdrawal_destination", length = 20)
    private String withdrawalDestination = "wallet"; // wallet, bank
    
    // Notification Preferences
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;
    
    @Column(name = "sms_notifications")
    private Boolean smsNotifications = false;
    
    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;
    
    @Column(name = "spend_save_notifications")
    private Boolean spendSaveNotifications = true;
    
    @Column(name = "interest_notifications")
    private Boolean interestNotifications = true;
    
    @Column(name = "milestone_notifications")
    private Boolean milestoneNotifications = true;
    
    // Security Preferences
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;
    
    @Column(name = "biometric_enabled")
    private Boolean biometricEnabled = false;
    
    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes = 30;
    
    // Privacy Preferences
    @Column(name = "data_sharing_enabled")
    private Boolean dataSharingEnabled = false;
    
    @Column(name = "marketing_emails")
    private Boolean marketingEmails = false;
    
    @Column(name = "analytics_tracking")
    private Boolean analyticsTracking = true;
    
    // Display Preferences
    @Column(name = "currency_display", length = 3)
    private String currencyDisplay = "NGN";
    
    @Column(name = "date_format", length = 20)
    private String dateFormat = "DD/MM/YYYY";
    
    @Column(name = "time_format", length = 10)
    private String timeFormat = "12"; // 12 or 24
    
    @Column(name = "theme", length = 20)
    private String theme = "light"; // light, dark, auto
    
    // Language Preferences
    @Column(name = "language", length = 5)
    private String language = "en";
    
    @Column(name = "timezone", length = 50)
    private String timezone = "Africa/Lagos";
    
    // Last Updated
    @Column(name = "last_settings_update")
    private LocalDateTime lastSettingsUpdate;
    
    @PreUpdate
    public void preUpdate() {
        this.lastSettingsUpdate = LocalDateTime.now();
    }
}