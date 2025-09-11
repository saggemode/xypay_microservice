package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserCleanupService.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired(required = false)
    private JavaMailSender javaMailSender;
    
    /**
     * Scheduled task to delete expired unverified users
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public String deleteExpiredUnverifiedUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<UserProfile> expiredProfiles = userProfileRepository.findByIsVerifiedFalseAndOtpExpiryBefore(now);
        
        int count = 0;
        StringBuilder deletedUsers = new StringBuilder();
        StringBuilder errorUsers = new StringBuilder();
        
        for (UserProfile profile : expiredProfiles) {
            User user = profile.getUser();
            try {
                logger.info("Deleting expired unverified user: {} ({})", user.getUsername(), user.getEmail());
                userProfileRepository.delete(profile);
                // User will be deleted via cascade or repository method
                deletedUsers.append(user.getUsername()).append(" (").append(user.getEmail()).append(")\n");
                count++;
            } catch (Exception e) {
                logger.error("Error deleting user {} ({}): {}", user.getUsername(), user.getEmail(), e.getMessage());
                errorUsers.append(user.getUsername()).append(" (").append(user.getEmail()).append("): ").append(e.getMessage()).append("\n");
            }
        }
        
        String result = String.format("Deleted %d expired unverified users. Errors: %d", count, errorUsers.length() > 0 ? 1 : 0);
        
        if (count > 0 || errorUsers.length() > 0) {
            StringBuilder message = new StringBuilder();
            if (deletedUsers.length() > 0) {
                message.append("The following users were deleted:\n").append(deletedUsers).append("\n");
            }
            if (errorUsers.length() > 0) {
                message.append("Errors occurred for the following users:\n").append(errorUsers);
            }
            
            sendAdminEmail(
                errorUsers.length() > 0 ? "Expired Unverified Users Deleted (with errors)" : "Expired Unverified Users Deleted",
                message.toString()
            );
            
            logger.info("Deleted {} expired unverified users. Errors: {}. Admins notified.", count, errorUsers.length() > 0 ? 1 : 0);
        } else {
            logger.info("No expired unverified users to delete.");
        }
        
        return result;
    }
    
    /**
     * Send email notification to admins
     * @param subject Email subject
     * @param message Email message
     */
    private void sendAdminEmail(String subject, String message) {
        try {
            if (javaMailSender != null) {
                // In a real application, you would get admin emails from configuration
                // For now, we'll just log the email
                logger.info("Admin email - Subject: {}, Message: {}", subject, message);
                
                // Uncomment and configure the following code in a real application:
                /*
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo("admin@bank.com"); // Get from configuration
                mailMessage.setSubject(subject);
                mailMessage.setText(message);
                javaMailSender.send(mailMessage);
                */
            }
        } catch (MailException e) {
            logger.error("Failed to send admin email: {}", e.getMessage());
        }
    }
}