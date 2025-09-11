package com.xypay.xypay.controller;

import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.repository.UserProfileRepository;
import com.xypay.xypay.service.EmailOtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class EmailVerificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationController.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private EmailOtpService emailOtpService;
    
    /**
     * Verify email with token
     * @param uid User profile ID
     * @param token Email verification token
     * @return Verification result
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String uid, @RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Parse Long ID
            Long userProfileId = Long.parseLong(uid);
            
            // Find user profile
            Optional<UserProfile> userProfileOpt = userProfileRepository.findById(userProfileId);
            
            if (userProfileOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid verification link");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserProfile userProfile = userProfileOpt.get();
            
            // Check if token is valid
            if (!userProfile.isEmailVerificationTokenValid(token)) {
                response.put("success", false);
                response.put("message", "Invalid or expired verification token");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verify email
            userProfile.verifyEmail();
            userProfileRepository.save(userProfile);
            
            response.put("success", true);
            response.put("message", "Email verified successfully");
            
            logger.info("Email verified successfully for user: {}", userProfile.getUser().getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid verification link format");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Email verification failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Email verification failed");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Resend email verification
     * @param request Request containing user email
     * @return Resend result
     */
    @PostMapping("/resend-verification-email")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Find user profile by email
            Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserEmail(email);
            
            if (userProfileOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserProfile userProfile = userProfileOpt.get();
            
            // Generate OTP
            String otp = emailOtpService.setOtp(userProfile);
            
            // Send verification email
            boolean emailSent = emailOtpService.sendOtpEmail(userProfile, otp);
            
            if (emailSent) {
                response.put("success", true);
                response.put("message", "Verification email sent successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to send verification email");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to resend verification email: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to resend verification email");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}