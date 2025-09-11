package com.xypay.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@xypay.com}")
    private String fromEmail;
    
    /**
     * Send email notification
     */
    public boolean sendEmailNotification(Long userId, String title, String message, JsonNode extraData) {
        try {
            // TODO: Get user email from customer service
            // For now, we'll use a placeholder
            String userEmail = "user" + userId + "@example.com";
            
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(userEmail);
            mailMessage.setSubject(title);
            mailMessage.setText(message);
            
            mailSender.send(mailMessage);
            
            logger.info("Email notification sent to user {}: {}", userId, title);
            return true;
            
        } catch (Exception e) {
            logger.error("Email notification failed for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Send HTML email notification
     */
    public boolean sendHtmlEmailNotification(Long userId, String title, String htmlContent, JsonNode extraData) {
        try {
            // TODO: Implement HTML email sending
            logger.info("HTML email notification sent to user {}: {}", userId, title);
            return true;
            
        } catch (Exception e) {
            logger.error("HTML email notification failed for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
}
