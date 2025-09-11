package com.xypay.xypay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending email notifications.
 */
@Service
public class EmailNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@xypay.com}")
    private String fromEmail;
    
    /**
     * Send email notification.
     *
     * @param to The recipient email address
     * @param subject The email subject
     * @param text The email text
     * @return true if successful, false otherwise
     */
    public boolean sendEmailNotification(String to, String subject, String text) {
        if (mailSender == null) {
            logger.warn("JavaMailSender not configured. Email to {} not sent: {}", to, subject);
            return false;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            logger.error("Email sending failed to {}: {}", to, e.getMessage());
            return false;
        }
    }
    
    /**
     * Send HTML email notification.
     *
     * @param to The recipient email address
     * @param subject The email subject
     * @param text The plain text email content
     * @param htmlText The HTML email content
     * @return true if successful, false otherwise
     */
    public boolean sendHtmlEmailNotification(String to, String subject, String text, String htmlText) {
        if (mailSender == null) {
            logger.warn("JavaMailSender not configured. HTML email to {} not sent: {}", to, subject);
            return false;
        }
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, htmlText); // text, html
            
            mailSender.send(mimeMessage);
            logger.info("HTML email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            logger.error("HTML email sending failed to {}: {}", to, e.getMessage());
            // Fallback to plain text email
            logger.info("Attempting fallback to plain text email for: {}", to);
            return sendEmailNotification(to, subject, text);
        }
    }
}