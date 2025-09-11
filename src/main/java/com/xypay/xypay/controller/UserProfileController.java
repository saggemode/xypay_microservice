package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.service.UserVerificationService;
import com.xypay.xypay.service.OTPService;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for user profile operations.
 * Equivalent to Django's user profile views.
 */
@RestController
@RequestMapping("/api/user")
public class UserProfileController {
    
    @Autowired
    private UserVerificationService userVerificationService;
    
    @Autowired
    private OTPService otpService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    /**
     * Register new user.
     * Equivalent to Django's register view.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userVerificationService.createUser(
                request.getUsername(), 
                request.getEmail(), 
                request.getPassword(), 
                request.getPhone()
            );
            
            response.put("success", true);
            response.put("message", "User registered successfully. Please verify with OTP.");
            response.put("username", user.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Verify user with OTP.
     * Equivalent to Django's verify view.
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestBody VerifyRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            boolean verified = userVerificationService.verifyUser(user, request.getOtp());
            
            if (verified) {
                response.put("success", true);
                response.put("message", "User verified successfully");
                response.put("verified", true);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired OTP");
                response.put("verified", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Verification failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Request new verification OTP.
     * Equivalent to Django's request_verification view.
     */
    @PostMapping("/request-verification")
    public ResponseEntity<Map<String, Object>> requestVerification(@RequestBody RequestVerificationRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            boolean sent = userVerificationService.requestVerification(user);
            
            if (sent) {
                response.put("success", true);
                response.put("message", "Verification OTP sent successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to send verification OTP");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Request failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get registration status.
     * Equivalent to Django's resume_registration view.
     */
    @GetMapping("/registration-status/{identifier}")
    public ResponseEntity<Map<String, Object>> getRegistrationStatus(@PathVariable String identifier) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            UserVerificationService.UserRegistrationStatus status = 
                userVerificationService.getRegistrationStatus(identifier);
            
            if (status != null) {
                response.put("success", true);
                response.put("status", status);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get user profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Use repository to avoid lazy loading issues
            UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
            if (profile == null) {
                response.put("success", false);
                response.put("message", "Profile not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("phone", profile.getPhone());
            profileData.put("verified", profile.getIsVerified());
            profileData.put("notify_email", profile.getNotifyEmail());
            profileData.put("notify_sms", profile.getNotifySms());
            profileData.put("notify_push", profile.getNotifyPush());
            profileData.put("notify_in_app", profile.getNotifyInApp());
            profileData.put("has_transaction_pin", profile.getTransactionPin() != null);
            
            response.put("success", true);
            response.put("profile", profileData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Update notification preferences.
     */
    @PutMapping("/notifications")
    public ResponseEntity<Map<String, Object>> updateNotifications(
            @RequestBody NotificationPreferencesRequest request, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Use repository to avoid lazy loading issues
            UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
            if (profile == null) {
                response.put("success", false);
                response.put("message", "Profile not found");
                return ResponseEntity.notFound().build();
            }
            
            profile.setNotifyEmail(request.getNotifyEmail());
            profile.setNotifySms(request.getNotifySms());
            profile.setNotifyPush(request.getNotifyPush());
            profile.setNotifyInApp(request.getNotifyInApp());
            
            userProfileRepository.save(profile);
            
            response.put("success", true);
            response.put("message", "Notification preferences updated");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Update failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Request DTOs
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String phone;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    public static class VerifyRequest {
        private String username;
        private String otp;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
    
    public static class RequestVerificationRequest {
        private String username;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
    
    public static class NotificationPreferencesRequest {
        private Boolean notifyEmail;
        private Boolean notifySms;
        private Boolean notifyPush;
        private Boolean notifyInApp;
        
        public Boolean getNotifyEmail() { return notifyEmail; }
        public void setNotifyEmail(Boolean notifyEmail) { this.notifyEmail = notifyEmail; }
        
        public Boolean getNotifySms() { return notifySms; }
        public void setNotifySms(Boolean notifySms) { this.notifySms = notifySms; }
        
        public Boolean getNotifyPush() { return notifyPush; }
        public void setNotifyPush(Boolean notifyPush) { this.notifyPush = notifyPush; }
        
        public Boolean getNotifyInApp() { return notifyInApp; }
        public void setNotifyInApp(Boolean notifyInApp) { this.notifyInApp = notifyInApp; }
    }
}
