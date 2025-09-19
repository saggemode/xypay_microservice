package com.xypay.xypay.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "alert_notification_configurations")
public class AlertNotificationConfiguration extends BaseEntity {
    
    @Column(name = "alert_name")
    private String alertName;
    
    @Column(name = "alert_type")
    private String alertType; // TRANSACTION, BALANCE, SUSPICIOUS_ACTIVITY, SYSTEM
    
    @Column(name = "notification_channels")
    private String notificationChannels; // JSON array of channels (SMS, EMAIL, IN_APP)
    
    @Column(name = "templates")
    private String templates; // JSON format of message templates for each channel
    
    @Column(name = "trigger_conditions")
    private String triggerConditions; // JSON format of conditions that trigger the alert
    
    @Column(name = "recipients")
    private String recipients; // JSON array of recipient user IDs or roles
    
    
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Getters and Setters

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getNotificationChannels() {
        return notificationChannels;
    }

    public void setNotificationChannels(String notificationChannels) {
        this.notificationChannels = notificationChannels;
    }

    public String getTemplates() {
        return templates;
    }

    public void setTemplates(String templates) {
        this.templates = templates;
    }

    public String getTriggerConditions() {
        return triggerConditions;
    }

    public void setTriggerConditions(String triggerConditions) {
        this.triggerConditions = triggerConditions;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }


    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}