package com.xypay.notification.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    // Email preferences
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;
    
    @Column(name = "email_transactions", nullable = false)
    private Boolean emailTransactions = true;
    
    @Column(name = "email_security", nullable = false)
    private Boolean emailSecurity = true;
    
    @Column(name = "email_marketing", nullable = false)
    private Boolean emailMarketing = false;
    
    @Column(name = "email_support", nullable = false)
    private Boolean emailSupport = true;
    
    @Column(name = "email_savings", nullable = false)
    private Boolean emailSavings = true;
    
    // SMS preferences
    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = true;
    
    @Column(name = "sms_transactions", nullable = false)
    private Boolean smsTransactions = true;
    
    @Column(name = "sms_security", nullable = false)
    private Boolean smsSecurity = true;
    
    @Column(name = "sms_marketing", nullable = false)
    private Boolean smsMarketing = false;
    
    @Column(name = "sms_support", nullable = false)
    private Boolean smsSupport = false;
    
    @Column(name = "sms_savings", nullable = false)
    private Boolean smsSavings = true;
    
    // Push notification preferences
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;
    
    @Column(name = "push_transactions", nullable = false)
    private Boolean pushTransactions = true;
    
    @Column(name = "push_security", nullable = false)
    private Boolean pushSecurity = true;
    
    @Column(name = "push_marketing", nullable = false)
    private Boolean pushMarketing = false;
    
    @Column(name = "push_support", nullable = false)
    private Boolean pushSupport = true;
    
    @Column(name = "push_savings", nullable = false)
    private Boolean pushSavings = true;
    
    // In-app notification preferences
    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;
    
    @Column(name = "in_app_transactions", nullable = false)
    private Boolean inAppTransactions = true;
    
    @Column(name = "in_app_security", nullable = false)
    private Boolean inAppSecurity = true;
    
    @Column(name = "in_app_marketing", nullable = false)
    private Boolean inAppMarketing = true;
    
    @Column(name = "in_app_support", nullable = false)
    private Boolean inAppSupport = true;
    
    @Column(name = "in_app_savings", nullable = false)
    private Boolean inAppSavings = true;
    
    // Frequency preferences
    @Column(name = "digest_frequency")
    private String digestFrequency = "DAILY"; // DAILY, WEEKLY, MONTHLY, NEVER
    
    @Column(name = "quiet_hours_start")
    private String quietHoursStart = "22:00";
    
    @Column(name = "quiet_hours_end")
    private String quietHoursEnd = "08:00";
    
    @Column(name = "timezone")
    private String timezone = "UTC";
    
    @Column(name = "language")
    private String language = "en";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public NotificationPreferences() {}
    
    public NotificationPreferences(Long userId) {
        this.userId = userId;
    }
    
    // Business Logic Methods
    public boolean isChannelEnabled(String channel) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return emailEnabled;
            case "SMS":
                return smsEnabled;
            case "PUSH":
                return pushEnabled;
            case "IN_APP":
                return inAppEnabled;
            default:
                return false;
        }
    }
    
    public boolean isNotificationTypeEnabled(String channel, String notificationType) {
        if (!isChannelEnabled(channel)) {
            return false;
        }
        
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return isEmailTypeEnabled(notificationType);
            case "SMS":
                return isSmsTypeEnabled(notificationType);
            case "PUSH":
                return isPushTypeEnabled(notificationType);
            case "IN_APP":
                return isInAppTypeEnabled(notificationType);
            default:
                return false;
        }
    }
    
    private boolean isEmailTypeEnabled(String notificationType) {
        switch (notificationType.toUpperCase()) {
            case "TRANSACTION":
            case "BANK_TRANSACTION":
            case "BANK_TRANSFER":
            case "BILL_PAYMENT":
            case "WALLET_CREDIT":
            case "WALLET_DEBIT":
                return emailTransactions;
            case "SECURITY":
            case "SECURITY_ALERT":
            case "PASSWORD_RESET":
            case "EMAIL_VERIFICATION":
                return emailSecurity;
            case "MARKETING":
            case "PROMOTION":
            case "FLASH_SALE":
                return emailMarketing;
            case "SUPPORT":
            case "NEW_MESSAGE":
                return emailSupport;
            case "SAVINGS":
            case "SPEND_AND_SAVE_ACTIVATION":
            case "AUTOMATIC_SAVE":
            case "SAVINGS_MILESTONE":
            case "TARGET_SAVING_CREATED":
            case "FIXED_SAVINGS_CREATED":
                return emailSavings;
            default:
                return true;
        }
    }
    
    private boolean isSmsTypeEnabled(String notificationType) {
        switch (notificationType.toUpperCase()) {
            case "TRANSACTION":
            case "BANK_TRANSACTION":
            case "BANK_TRANSFER":
            case "BILL_PAYMENT":
            case "WALLET_CREDIT":
            case "WALLET_DEBIT":
                return smsTransactions;
            case "SECURITY":
            case "SECURITY_ALERT":
            case "PASSWORD_RESET":
            case "EMAIL_VERIFICATION":
                return smsSecurity;
            case "MARKETING":
            case "PROMOTION":
            case "FLASH_SALE":
                return smsMarketing;
            case "SUPPORT":
            case "NEW_MESSAGE":
                return smsSupport;
            case "SAVINGS":
            case "SPEND_AND_SAVE_ACTIVATION":
            case "AUTOMATIC_SAVE":
            case "SAVINGS_MILESTONE":
            case "TARGET_SAVING_CREATED":
            case "FIXED_SAVINGS_CREATED":
                return smsSavings;
            default:
                return true;
        }
    }
    
    private boolean isPushTypeEnabled(String notificationType) {
        switch (notificationType.toUpperCase()) {
            case "TRANSACTION":
            case "BANK_TRANSACTION":
            case "BANK_TRANSFER":
            case "BILL_PAYMENT":
            case "WALLET_CREDIT":
            case "WALLET_DEBIT":
                return pushTransactions;
            case "SECURITY":
            case "SECURITY_ALERT":
            case "PASSWORD_RESET":
            case "EMAIL_VERIFICATION":
                return pushSecurity;
            case "MARKETING":
            case "PROMOTION":
            case "FLASH_SALE":
                return pushMarketing;
            case "SUPPORT":
            case "NEW_MESSAGE":
                return pushSupport;
            case "SAVINGS":
            case "SPEND_AND_SAVE_ACTIVATION":
            case "AUTOMATIC_SAVE":
            case "SAVINGS_MILESTONE":
            case "TARGET_SAVING_CREATED":
            case "FIXED_SAVINGS_CREATED":
                return pushSavings;
            default:
                return true;
        }
    }
    
    private boolean isInAppTypeEnabled(String notificationType) {
        switch (notificationType.toUpperCase()) {
            case "TRANSACTION":
            case "BANK_TRANSACTION":
            case "BANK_TRANSFER":
            case "BILL_PAYMENT":
            case "WALLET_CREDIT":
            case "WALLET_DEBIT":
                return inAppTransactions;
            case "SECURITY":
            case "SECURITY_ALERT":
            case "PASSWORD_RESET":
            case "EMAIL_VERIFICATION":
                return inAppSecurity;
            case "MARKETING":
            case "PROMOTION":
            case "FLASH_SALE":
                return inAppMarketing;
            case "SUPPORT":
            case "NEW_MESSAGE":
                return inAppSupport;
            case "SAVINGS":
            case "SPEND_AND_SAVE_ACTIVATION":
            case "AUTOMATIC_SAVE":
            case "SAVINGS_MILESTONE":
            case "TARGET_SAVING_CREATED":
            case "FIXED_SAVINGS_CREATED":
                return inAppSavings;
            default:
                return true;
        }
    }
    
    public boolean isInQuietHours() {
        // Simple implementation - can be enhanced with timezone support
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        int startHour = Integer.parseInt(quietHoursStart.split(":")[0]);
        int endHour = Integer.parseInt(quietHoursEnd.split(":")[0]);
        
        if (startHour > endHour) {
            // Quiet hours span midnight
            return currentHour >= startHour || currentHour < endHour;
        } else {
            // Quiet hours within same day
            return currentHour >= startHour && currentHour < endHour;
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Boolean getEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    
    public Boolean getEmailTransactions() { return emailTransactions; }
    public void setEmailTransactions(Boolean emailTransactions) { this.emailTransactions = emailTransactions; }
    
    public Boolean getEmailSecurity() { return emailSecurity; }
    public void setEmailSecurity(Boolean emailSecurity) { this.emailSecurity = emailSecurity; }
    
    public Boolean getEmailMarketing() { return emailMarketing; }
    public void setEmailMarketing(Boolean emailMarketing) { this.emailMarketing = emailMarketing; }
    
    public Boolean getEmailSupport() { return emailSupport; }
    public void setEmailSupport(Boolean emailSupport) { this.emailSupport = emailSupport; }
    
    public Boolean getEmailSavings() { return emailSavings; }
    public void setEmailSavings(Boolean emailSavings) { this.emailSavings = emailSavings; }
    
    public Boolean getSmsEnabled() { return smsEnabled; }
    public void setSmsEnabled(Boolean smsEnabled) { this.smsEnabled = smsEnabled; }
    
    public Boolean getSmsTransactions() { return smsTransactions; }
    public void setSmsTransactions(Boolean smsTransactions) { this.smsTransactions = smsTransactions; }
    
    public Boolean getSmsSecurity() { return smsSecurity; }
    public void setSmsSecurity(Boolean smsSecurity) { this.smsSecurity = smsSecurity; }
    
    public Boolean getSmsMarketing() { return smsMarketing; }
    public void setSmsMarketing(Boolean smsMarketing) { this.smsMarketing = smsMarketing; }
    
    public Boolean getSmsSupport() { return smsSupport; }
    public void setSmsSupport(Boolean smsSupport) { this.smsSupport = smsSupport; }
    
    public Boolean getSmsSavings() { return smsSavings; }
    public void setSmsSavings(Boolean smsSavings) { this.smsSavings = smsSavings; }
    
    public Boolean getPushEnabled() { return pushEnabled; }
    public void setPushEnabled(Boolean pushEnabled) { this.pushEnabled = pushEnabled; }
    
    public Boolean getPushTransactions() { return pushTransactions; }
    public void setPushTransactions(Boolean pushTransactions) { this.pushTransactions = pushTransactions; }
    
    public Boolean getPushSecurity() { return pushSecurity; }
    public void setPushSecurity(Boolean pushSecurity) { this.pushSecurity = pushSecurity; }
    
    public Boolean getPushMarketing() { return pushMarketing; }
    public void setPushMarketing(Boolean pushMarketing) { this.pushMarketing = pushMarketing; }
    
    public Boolean getPushSupport() { return pushSupport; }
    public void setPushSupport(Boolean pushSupport) { this.pushSupport = pushSupport; }
    
    public Boolean getPushSavings() { return pushSavings; }
    public void setPushSavings(Boolean pushSavings) { this.pushSavings = pushSavings; }
    
    public Boolean getInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(Boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }
    
    public Boolean getInAppTransactions() { return inAppTransactions; }
    public void setInAppTransactions(Boolean inAppTransactions) { this.inAppTransactions = inAppTransactions; }
    
    public Boolean getInAppSecurity() { return inAppSecurity; }
    public void setInAppSecurity(Boolean inAppSecurity) { this.inAppSecurity = inAppSecurity; }
    
    public Boolean getInAppMarketing() { return inAppMarketing; }
    public void setInAppMarketing(Boolean inAppMarketing) { this.inAppMarketing = inAppMarketing; }
    
    public Boolean getInAppSupport() { return inAppSupport; }
    public void setInAppSupport(Boolean inAppSupport) { this.inAppSupport = inAppSupport; }
    
    public Boolean getInAppSavings() { return inAppSavings; }
    public void setInAppSavings(Boolean inAppSavings) { this.inAppSavings = inAppSavings; }
    
    public String getDigestFrequency() { return digestFrequency; }
    public void setDigestFrequency(String digestFrequency) { this.digestFrequency = digestFrequency; }
    
    public String getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(String quietHoursStart) { this.quietHoursStart = quietHoursStart; }
    
    public String getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(String quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
