package com.xypay.xypay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS notification service using Twilio.
 * Equivalent to Django's send_sms_notification function.
 */
@Service
public class SMSNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SMSNotificationService.class);
    
    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;
    
    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;
    
    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;
    
    private boolean twilioConfigured = false;
    
    /**
     * Send SMS notification using Twilio.
     * Equivalent to Django's send_sms_notification function.
     *
     * @param phoneNumber The recipient's phone number
     * @param message The message to send
     * @return true if successful, false otherwise
     */
    public boolean sendSMSNotification(String phoneNumber, String message) {
        if (!isTwilioConfigured()) {
            logger.warn("SMS notification skipped - Twilio not configured");
            return false;
        }
        
        try {
            // TODO: Implement actual Twilio integration
            // Example implementation:
            // Twilio.init(twilioAccountSid, twilioAuthToken);
            // Message twilioMessage = Message.creator(
            //     new PhoneNumber(phoneNumber),
            //     new PhoneNumber(twilioPhoneNumber),
            //     message
            // ).create();
            // logger.info("SMS sent successfully: {}", twilioMessage.getSid());
            
            // For now, log the SMS (mock implementation)
            logger.info("[SMS] To: {} | Message: {}", phoneNumber, message);
            return true;
            
        } catch (Exception e) {
            logger.error("SMS sending failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if Twilio is properly configured
     */
    private boolean isTwilioConfigured() {
        if (twilioConfigured) {
            return true;
        }
        
        if (twilioAccountSid != null && !twilioAccountSid.isEmpty() &&
            twilioAuthToken != null && !twilioAuthToken.isEmpty() &&
            twilioPhoneNumber != null && !twilioPhoneNumber.isEmpty()) {
            twilioConfigured = true;
            logger.info("Twilio SMS service configured successfully");
            return true;
        }
        
        return false;
    }
}