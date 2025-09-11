package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.repository.UserProfileRepository;
import com.xypay.xypay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionPinService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionPinService.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    /**
     * Set transaction PIN for user (first time setup)
     */
    @Transactional
    public Map<String, Object> setTransactionPin(Long userId, String pin) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate PIN
            if (!isValidPin(pin)) {
                response.put("success", false);
                response.put("message", "PIN must be 4-10 digits");
                return response;
            }
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            UserProfile profile = user.getProfile();
            if (profile == null) {
                response.put("success", false);
                response.put("message", "User profile not found");
                return response;
            }
            
            // Check if PIN already exists
            if (profile.getTransactionPin() != null) {
                response.put("success", false);
                response.put("message", "Transaction PIN already set. Use update PIN instead.");
                return response;
            }
            
            // Set the PIN (hashed)
            profile.setTransactionPinHashed(pin);
            userProfileRepository.save(profile);
            
            auditTrailService.logEvent("TRANSACTION_PIN_SET", 
                String.format("Transaction PIN set for user: %s", profile.getUser().getUsername()));
            
            response.put("success", true);
            response.put("message", "Transaction PIN set successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to set transaction PIN: " + e.getMessage());
            
            logger.error("Failed to set PIN for user ID: {}, error: {}", userId, e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Update transaction PIN (requires old PIN verification)
     */
    @Transactional
    public Map<String, Object> updateTransactionPin(Long userId, String oldPin, String newPin) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate new PIN
            if (!isValidPin(newPin)) {
                response.put("success", false);
                response.put("message", "New PIN must be 4-10 digits");
                return response;
            }
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            UserProfile profile = user.getProfile();
            if (profile == null) {
                response.put("success", false);
                response.put("message", "User profile not found");
                return response;
            }
            
            // Check if PIN exists
            if (profile.getTransactionPin() == null) {
                response.put("success", false);
                response.put("message", "No transaction PIN set. Use set PIN instead.");
                return response;
            }
            
            // Verify old PIN
            if (!profile.checkTransactionPin(oldPin)) {
                response.put("success", false);
                response.put("message", "Invalid old PIN");
                
                auditTrailService.logEvent("TRANSACTION_PIN_FAILED", 
                    String.format("Invalid old PIN attempt for user: %s", profile.getUser().getUsername()));
                return response;
            }
            
            // Update to new PIN (hashed)
            profile.setTransactionPinHashed(newPin);
            userProfileRepository.save(profile);
            
            auditTrailService.logEvent("TRANSACTION_PIN_UPDATED", 
                String.format("Transaction PIN updated for user: %s", profile.getUser().getUsername()));
            
            response.put("success", true);
            response.put("message", "Transaction PIN updated successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update transaction PIN: " + e.getMessage());
            
            logger.error("Failed to update PIN for user ID: {}, error: {}", userId, e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Verify transaction PIN
     */
    public Map<String, Object> verifyTransactionPin(Long userId, String pin) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            UserProfile profile = user.getProfile();
            if (profile == null) {
                response.put("success", false);
                response.put("message", "User profile not found");
                return response;
            }
            
            if (profile.getTransactionPin() == null) {
                response.put("success", false);
                response.put("message", "No transaction PIN set");
                return response;
            }
            
            boolean isValid = profile.checkTransactionPin(pin);
            
            if (isValid) {
                auditTrailService.logEvent("TRANSACTION_PIN_VERIFIED", 
                    String.format("Transaction PIN verified for user: %s", profile.getUser().getUsername()));
            } else {
                auditTrailService.logEvent("TRANSACTION_PIN_FAILED", 
                    String.format("Invalid PIN attempt for user: %s", profile.getUser().getUsername()));
            }
            
            response.put("success", isValid);
            response.put("message", isValid ? "PIN verified successfully" : "Invalid PIN");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to verify transaction PIN: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Check if user has transaction PIN set
     */
    public Map<String, Object> hasTransactionPin(Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            UserProfile profile = user.getProfile();
            if (profile == null) {
                response.put("success", false);
                response.put("message", "User profile not found");
                return response;
            }
            
            boolean hasPin = profile.getTransactionPin() != null;
            
            response.put("success", true);
            response.put("has_pin", hasPin);
            response.put("message", hasPin ? "Transaction PIN is set" : "No transaction PIN set");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to check transaction PIN status: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Reset transaction PIN (Admin function - requires additional verification)
     */
    @Transactional
    public Map<String, Object> resetTransactionPin(Long userId, Long adminId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            UserProfile profile = user.getProfile();
            if (profile == null) {
                response.put("success", false);
                response.put("message", "User profile not found");
                return response;
            }
            
            // Clear the PIN
            profile.setTransactionPin(null);
            userProfileRepository.save(profile);
            
            auditTrailService.logEvent("TRANSACTION_PIN_RESET", 
                String.format("Transaction PIN reset for user: %s by admin ID: %d", 
                    profile.getUser().getUsername(), adminId));
            
            response.put("success", true);
            response.put("message", "Transaction PIN reset successfully. User can now set a new PIN.");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to reset transaction PIN: " + e.getMessage());
            
            logger.error("Failed to reset PIN for user ID: {} by admin ID: {}, error: {}", 
                userId, adminId, e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Validate PIN format
     */
    private boolean isValidPin(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            return false;
        }
        
        // Check if PIN is numeric
        if (!pin.matches("\\d+")) {
            return false;
        }
        
        // Check length (4-10 digits)
        int length = pin.length();
        return length >= 4 && length <= 10;
    }
}