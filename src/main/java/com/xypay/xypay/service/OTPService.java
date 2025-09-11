package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service for OTP generation, validation, and sending.
 * Equivalent to Django's OTP utility functions.
 */
@Service
public class OTPService {
    
    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);
    private static final SecureRandom random = new SecureRandom();
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.site.url:http://localhost:8080}")
    private String siteUrl;
    
    /**
     * Generate a 6-digit OTP.
     * Equivalent to Django's generate_otp().
     */
    public String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * Set OTP and expiry on user profile.
     * Equivalent to Django's set_otp().
     */
    public String setOtp(UserProfile profile) {
        String otp = generateOtp();
        profile.setOtpCode(otp);
        profile.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        userProfileRepository.save(profile);
        
        logger.info("OTP set for user {} - expires in {} minutes", 
            profile.getUser().getUsername(), otpExpiryMinutes);
        
        return otp;
    }
    
    /**
     * Send OTP via email.
     * Equivalent to Django's send_otp_email().
     */
    public boolean sendOtpEmail(User user, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Your OTP Code for Account Verification");
            
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Your OTP code is: %s\n" +
                "This OTP will expire in %d minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "XyPay Team",
                user.getUsername(), otp, otpExpiryMinutes
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            logger.info("OTP email sent to {}", user.getEmail());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", user.getEmail(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Send OTP via SMS (placeholder implementation).
     * Equivalent to Django's send_otp_sms().
     */
    public boolean sendOtpSms(String phone, String otp) {
        try {
            // TODO: Implement actual SMS service integration (Twilio, etc.)
            logger.info("SMS OTP would be sent to {}: {}", phone, otp);
            
            // Placeholder - in production, integrate with SMS service
            // Example with Twilio:
            // Twilio.init(accountSid, authToken);
            // Message message = Message.creator(
            //     new PhoneNumber(phone),
            //     new PhoneNumber(twilioPhoneNumber),
            //     "Your XyPay OTP code is: " + otp
            // ).create();
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send OTP SMS to {}: {}", phone, e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate OTP for a user profile.
     */
    public boolean validateOtp(UserProfile profile, String otp) {
        if (profile.getOtpCode() == null || profile.getOtpExpiry() == null) {
            logger.warn("No OTP set for user {}", profile.getUser().getUsername());
            return false;
        }
        
        if (LocalDateTime.now().isAfter(profile.getOtpExpiry())) {
            logger.warn("OTP expired for user {}", profile.getUser().getUsername());
            return false;
        }
        
        boolean isValid = otp.equals(profile.getOtpCode());
        
        if (isValid) {
            logger.info("OTP validated successfully for user {}", profile.getUser().getUsername());
            // Clear OTP after successful validation
            profile.clearOtp();
            userProfileRepository.save(profile);
        } else {
            logger.warn("Invalid OTP provided for user {}", profile.getUser().getUsername());
        }
        
        return isValid;
    }
    
    /**
     * Clear expired OTPs from the system.
     */
    public int clearExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        int cleared = userProfileRepository.clearExpiredOtps(now);
        
        if (cleared > 0) {
            logger.info("Cleared {} expired OTPs", cleared);
        }
        
        return cleared;
    }
}
