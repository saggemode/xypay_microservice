package com.xypay.xypay.config;

import org.springframework.context.annotation.Configuration;

import lombok.Data;
import java.time.LocalDateTime;


@Data
@Configuration
public class AlertNotificationConfiguration {
    
    private Long id;
    
    private String alertName;
    
    private String alertType; // TRANSACTION, BALANCE, SUSPICIOUS_ACTIVITY, SYSTEM
    
    private String notificationChannels; // JSON array of channels (SMS, EMAIL, IN_APP)
    
    private String templates; // JSON format of message templates for each channel
    
    private String triggerConditions; // JSON format of conditions that trigger the alert
    
    private String recipients; // JSON array of recipient user IDs or roles
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Boolean isActive = true;
    
    // Configuration class for alert notification settings
}