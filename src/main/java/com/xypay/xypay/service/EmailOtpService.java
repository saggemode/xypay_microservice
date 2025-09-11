package com.xypay.xypay.service;

import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailOtpService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailOtpService.class);
    private static final SecureRandom random = new SecureRandom();
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    
    @Value("${app.site-url:http://localhost:8080}")
    private String siteUrl;
    
    @Value("${app.otp-expiry-minutes:10}")
    private int otpExpiryMinutes;
    
    /**
     * Generate a 6-digit OTP
     * @return 6-digit OTP as string
     */
    public String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * Send OTP via email with HTML verification link
     * @param userProfile User profile to send OTP to
     * @param otp OTP code to send
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendOtpEmail(UserProfile userProfile, String otp) {
        try {
            String username = userProfile.getUser().getUsername();
            String email = userProfile.getUser().getEmail();
            
            // Generate email verification token
            userProfile.generateEmailVerificationToken();
            userProfileRepository.save(userProfile);
            
            String verificationLink = String.format("%s/api/auth/verify-email/?uid=%s&token=%s", 
                    siteUrl, 
                    userProfile.getId().toString(), 
                    userProfile.getEmailVerificationToken());
            
            // Create simple email content (no template engine needed)
            String subject = "Your OTP Code for Account Verification";
            
            String htmlContent = String.format(
                "<html><body>" +
                "<h2>Hello %s,</h2>" +
                "<p>Your OTP code is: <strong>%s</strong></p>" +
                "<p>This OTP will expire in %d minutes.</p>" +
                "<p>Or <a href='%s'>click here to verify your account</a></p>" +
                "<p>If you did not request this, please ignore this email.</p>" +
                "</body></html>",
                username, otp, otpExpiryMinutes, verificationLink);
            
            String textContent = String.format(
                "Hello %s,\n\n" +
                "Your OTP code is: %s\n" +
                "This OTP will expire in %d minutes.\n\n" +
                "Or click the link below to verify your account:\n%s\n\n" +
                "If you did not request this, please ignore this email.",
                username, otp, otpExpiryMinutes, verificationLink);
            
            // Send email using NotificationService
            return notificationService.sendHtmlEmail(email, subject, htmlContent, textContent);
            
        } catch (Exception e) {
            logger.error("Failed to send OTP email: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send OTP via SMS (placeholder implementation)
     * @param phone Phone number to send OTP to
     * @param otp OTP code to send
     * @return true if SMS was sent successfully, false otherwise
     */
    public boolean sendOtpSms(String phone, String otp) {
        // In production, integrate with SMS service like Twilio
        // Mock SMS implementation - logs instead of sending
        logger.info("[MOCK SMS] OTP {} sent to {}", otp, phone);
        return true;
    }
    
    /**
     * Generate and set OTP for user verification
     * @param userProfile User profile to set OTP for
     * @return generated OTP
     */
    public String setOtp(UserProfile userProfile) {
        String otp = generateOtp();
        userProfile.setOtpCode(otp);
        userProfile.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        userProfileRepository.save(userProfile);
        return otp;
    }
    
    /**
     * Check if OTP is valid for the given user profile
     * @param userProfile User profile to check OTP for
     * @param otp OTP code to validate
     * @return true if OTP is valid, false otherwise
     */
    public boolean isOtpValid(UserProfile userProfile, String otp) {
        return userProfile.getOtpCode() != null && 
               userProfile.getOtpCode().equals(otp) && 
               userProfile.getOtpExpiry() != null && 
               LocalDateTime.now().isBefore(userProfile.getOtpExpiry());
    }
}