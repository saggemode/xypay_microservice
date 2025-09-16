package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service for handling two-factor authentication.
 */
@Service
public class TwoFactorAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(TwoFactorAuthService.class);
    
    
    /**
     * Generate a random 6-digit 2FA code.
     * 
     * @return 6-digit code
     */
    public String generate2faCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * Send 2FA code to user via SMS and/or email.
     * 
     * @param user User to send code to
     * @param code Generated 2FA code
     * @param transferAmount Amount being transferred
     * @param recipientAccount Recipient account number
     */
    public void send2faCode(User user, String code, java.math.BigDecimal transferAmount, String recipientAccount) {
        try {
            // Format message
            String message = String.format(
                "Your verification code for transfer of %s NGN to account %s is: %s\n" +
                "This code expires in 10 minutes.",
                transferAmount, recipientAccount.substring(recipientAccount.length() - 4), code
            );
            
            // Send SMS if phone number exists
            String userPhone = getUserPhone(user);
            if (userPhone != null && !userPhone.isEmpty()) {
                sendSms(userPhone, message);
            }
            
            // Send email
            sendEmail(user.getEmail(), "Transfer Verification Code", message);
            
            logger.info("2FA code sent to user {} via SMS/email", user.getId());
            
        } catch (Exception e) {
            logger.error("Error sending 2FA code: {}", e.getMessage());
            throw new RuntimeException("Failed to send 2FA code", e);
        }
    }
    
    /**
     * Verify a 2FA code for a transfer.
     * 
     * @param transfer Transfer being verified
     * @param code Code to verify
     * @return True if code is valid
     */
    public boolean verify2faCode(BankTransfer transfer, String code) {
        try {
            if (transfer.getTwoFaCode() == null || transfer.getTwoFaExpiresAt() == null) {
                logger.error("No 2FA code found for transfer {}", transfer.getId());
                return false;
            }
            
            // Check if code has expired
            if (LocalDateTime.now().isAfter(transfer.getTwoFaExpiresAt())) {
                logger.warn("2FA code expired for transfer {}", transfer.getId());
                return false;
            }
            
            // Verify code
            boolean isValid = transfer.getTwoFaCode().equals(code);
            
            if (isValid) {
                logger.info("2FA code verified for transfer {}", transfer.getId());
            } else {
                logger.warn("Invalid 2FA code provided for transfer {}", transfer.getId());
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error verifying 2FA code: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send SMS via SMS gateway/provider.
     * 
     * @param phoneNumber Recipient phone number
     * @param message Message to send
     */
    public void sendSms(String phoneNumber, String message) {
        try {
            // In production, integrate with SMS gateway
            // For now, just log the message
            logger.info("[MOCK SMS] To: {}, Message: {}", phoneNumber, message);
            
            // In production, you would call your SMS service here
            // smsNotificationService.sendSMSNotification(phoneNumber, message);
            
        } catch (Exception e) {
            logger.error("Error sending SMS: {}", e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
    
    /**
     * Send email via email service.
     * 
     * @param email Recipient email
     * @param subject Email subject
     * @param message Email message
     */
    public void sendEmail(String email, String subject, String message) {
        try {
            // In production, use Spring's email service or email service
            // For now, just log the message
            logger.info("[MOCK EMAIL] To: {}, Subject: {}, Message: {}", email, subject, message);
            
            // In production, you would call your email service here
            // emailNotificationService.sendEmailNotification(email, subject, message);
            
        } catch (Exception e) {
            logger.error("Error sending email: {}", e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Get user phone number from profile.
     * 
     * @param user User to get phone for
     * @return Phone number or null if not found
     */
    private String getUserPhone(User user) {
        try {
            // You'll need to implement this based on your UserProfile structure
            // For now, return null as placeholder
            return null;
        } catch (Exception e) {
            logger.warn("Error getting phone for user {}: {}", user.getId(), e.getMessage());
            return null;
        }
    }
}
