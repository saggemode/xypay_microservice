package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling transaction PIN operations including
 * creation, validation, reset, and security measures.
 */
@Service
public class TransactionPinService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionPinService.class);
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    private static final int PIN_LENGTH = 4; // Standard 4-digit PIN
    private static final int MAX_ATTEMPTS = 3; // Maximum failed attempts before temporary lockout
    private static final int LOCKOUT_DURATION_MINUTES = 30; // Lockout duration in minutes
    private static final int PBKDF2_ITERATIONS = 100000; // Number of iterations for PBKDF2
    
    /**
     * Create or update a user's transaction PIN.
     * 
     * @param user User to set PIN for
     * @param pin New PIN to set
     * @return Creation result
     */
    public Map<String, Object> createTransactionPin(User user, String pin) {
        try {
            if (pin == null || !pin.matches("\\d{" + PIN_LENGTH + "}")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "PIN must be " + PIN_LENGTH + " digits");
                return result;
            }
            
            // Hash the PIN before storing
            byte[] salt = generateSalt();
            byte[] pinHash = hashPin(pin, salt);
            
            // Store PIN hash and salt in user profile
            // Note: You'll need to add these fields to your User entity or UserProfile
            // For now, we'll simulate the storage
            storePinData(user, pinHash, salt);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Transaction PIN created successfully");
            return result;
            
        } catch (Exception e) {
            logger.error("Error creating transaction PIN: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Failed to create transaction PIN");
            return result;
        }
    }
    
    /**
     * Verify a transaction PIN.
     * 
     * @param user User to verify PIN for
     * @param pin PIN to verify
     * @return Verification result
     */
    public Map<String, Object> verifyTransactionPin(User user, String pin) {
        try {
            // Check if PIN is locked
            if (isPinLocked(user)) {
                int remainingTime = getRemainingLockoutTime(user);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "PIN is locked. Try again in " + remainingTime + " minutes");
                result.put("is_locked", true);
                result.put("remaining_time", remainingTime);
                return result;
            }
            
            // Get stored PIN data
            PinData pinData = getPinData(user);
            if (pinData == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "No PIN set for user");
                return result;
            }
            
            // Verify the PIN
            byte[] inputPinHash = hashPin(pin, pinData.salt);
            boolean isValid = java.util.Arrays.equals(inputPinHash, pinData.pinHash);
            
            if (isValid) {
                // Reset failed attempts on successful verification
                resetFailedAttempts(user);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "PIN verified successfully");
                return result;
            } else {
                // Increment failed attempts
                int failedAttempts = incrementFailedAttempts(user);
                
                // Check if should lock
                if (failedAttempts >= MAX_ATTEMPTS) {
                    lockPin(user);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", "PIN locked for " + LOCKOUT_DURATION_MINUTES + 
                        " minutes due to too many failed attempts");
                    result.put("is_locked", true);
                    result.put("remaining_time", LOCKOUT_DURATION_MINUTES);
                    return result;
                }
                
                int remainingAttempts = MAX_ATTEMPTS - failedAttempts;
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Invalid PIN. " + remainingAttempts + " attempts remaining");
                result.put("remaining_attempts", remainingAttempts);
                return result;
            }
            
        } catch (Exception e) {
            logger.error("Error verifying transaction PIN: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Failed to verify transaction PIN");
            return result;
        }
    }
    
    /**
     * Reset a transaction PIN.
     * 
     * @param user User to reset PIN for
     * @param oldPin Current PIN
     * @param newPin New PIN to set
     * @return Reset result
     */
    public Map<String, Object> resetTransactionPin(User user, String oldPin, String newPin) {
        try {
            // First verify the old PIN
            Map<String, Object> verifyResult = verifyTransactionPin(user, oldPin);
            if (!(Boolean) verifyResult.get("success")) {
                return verifyResult;
            }
            
            // Then create new PIN
            return createTransactionPin(user, newPin);
            
        } catch (Exception e) {
            logger.error("Error resetting transaction PIN: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Failed to reset transaction PIN");
            return result;
        }
    }
    
    /**
     * Request a PIN reset (for forgotten PINs).
     * This will trigger a verification process (e.g., SMS, email).
     * 
     * @param user User requesting PIN reset
     * @return Request result
     */
    public Map<String, Object> requestPinReset(User user) {
        try {
            // Generate reset token
            String resetToken = UUID.randomUUID().toString().replace("-", "");
            LocalDateTime resetExpires = LocalDateTime.now().plusMinutes(30);
            
            // Save token and expiry
            storeResetToken(user, resetToken, resetExpires);
            
            // Send reset instructions via SMS and email
            String message = String.format(
                "You have requested to reset your transaction PIN.\n" +
                "Your reset code is: %s\n" +
                "This code will expire in 30 minutes.",
                resetToken.substring(0, 6)
            );
            
            // Send via SMS if phone number exists
            String userPhone = getUserPhone(user);
            if (userPhone != null && !userPhone.isEmpty()) {
                twoFactorAuthService.sendSms(userPhone, message);
            }
            
            // Send via email
            twoFactorAuthService.sendEmail(user.getEmail(), "Transaction PIN Reset Request", message);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "PIN reset instructions sent");
            result.put("expires_in", "30 minutes");
            return result;
            
        } catch (Exception e) {
            logger.error("Error requesting PIN reset: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Failed to process PIN reset request");
            return result;
        }
    }
    
    /**
     * Verify PIN reset token and set new PIN.
     * 
     * @param user User resetting PIN
     * @param resetToken Reset verification token
     * @param newPin New PIN to set
     * @return Reset verification result
     */
    public Map<String, Object> verifyPinReset(User user, String resetToken, String newPin) {
        try {
            // Get stored reset token data
            ResetTokenData tokenData = getResetTokenData(user);
            if (tokenData == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "No active PIN reset request");
                return result;
            }
            
            if (LocalDateTime.now().isAfter(tokenData.expiresAt)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "PIN reset token has expired");
                return result;
            }
            
            if (!java.security.MessageDigest.isEqual(
                resetToken.getBytes(), tokenData.token.getBytes())) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Invalid reset token");
                return result;
            }
            
            // Clear reset token and set new PIN
            clearResetToken(user);
            
            return createTransactionPin(user, newPin);
            
        } catch (Exception e) {
            logger.error("Error verifying PIN reset: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Failed to verify PIN reset");
            return result;
        }
    }
    
    /**
     * Generate a random salt for PIN hashing.
     */
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return salt;
    }
    
    /**
     * Hash a PIN using PBKDF2 with the given salt.
     */
    private byte[] hashPin(String pin, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }
    
    /**
     * Store PIN data for a user.
     * Note: This is a placeholder - you'll need to implement actual storage
     */
    private void storePinData(User user, byte[] pinHash, byte[] salt) {
        // TODO: Implement actual storage in your User entity or UserProfile
        // For now, this is a placeholder
        logger.debug("Storing PIN data for user {}", user.getId());
    }
    
    /**
     * Get PIN data for a user.
     * Note: This is a placeholder - you'll need to implement actual retrieval
     */
    private PinData getPinData(User user) {
        // TODO: Implement actual retrieval from your User entity or UserProfile
        // For now, return null as placeholder
        return null;
    }
    
    /**
     * Check if PIN is locked for a user.
     * Note: This is a placeholder - you'll need to implement actual checking
     */
    private boolean isPinLocked(User user) {
        // TODO: Implement actual checking from your User entity or UserProfile
        return false;
    }
    
    /**
     * Get remaining lockout time for a user.
     * Note: This is a placeholder - you'll need to implement actual calculation
     */
    private int getRemainingLockoutTime(User user) {
        // TODO: Implement actual calculation from your User entity or UserProfile
        return 0;
    }
    
    /**
     * Reset failed attempts for a user.
     * Note: This is a placeholder - you'll need to implement actual reset
     */
    private void resetFailedAttempts(User user) {
        // TODO: Implement actual reset in your User entity or UserProfile
        logger.debug("Resetting failed attempts for user {}", user.getId());
    }
    
    /**
     * Increment failed attempts for a user.
     * Note: This is a placeholder - you'll need to implement actual increment
     */
    private int incrementFailedAttempts(User user) {
        // TODO: Implement actual increment in your User entity or UserProfile
        return 0;
    }
    
    /**
     * Lock PIN for a user.
     * Note: This is a placeholder - you'll need to implement actual locking
     */
    private void lockPin(User user) {
        // TODO: Implement actual locking in your User entity or UserProfile
        logger.debug("Locking PIN for user {}", user.getId());
    }
    
    /**
     * Store reset token for a user.
     * Note: This is a placeholder - you'll need to implement actual storage
     */
    private void storeResetToken(User user, String token, LocalDateTime expiresAt) {
        // TODO: Implement actual storage in your User entity or UserProfile
        logger.debug("Storing reset token for user {}", user.getId());
    }
    
    /**
     * Get reset token data for a user.
     * Note: This is a placeholder - you'll need to implement actual retrieval
     */
    private ResetTokenData getResetTokenData(User user) {
        // TODO: Implement actual retrieval from your User entity or UserProfile
        return null;
    }
    
    /**
     * Clear reset token for a user.
     * Note: This is a placeholder - you'll need to implement actual clearing
     */
    private void clearResetToken(User user) {
        // TODO: Implement actual clearing in your User entity or UserProfile
        logger.debug("Clearing reset token for user {}", user.getId());
    }
    
    /**
     * Get user phone number from profile.
     * Note: This is a placeholder - you'll need to implement actual retrieval
     */
    private String getUserPhone(User user) {
        // TODO: Implement actual retrieval from your User entity or UserProfile
        return null;
    }
    
    /**
     * Inner class to hold PIN data.
     */
    private static class PinData {
        final byte[] pinHash;
        final byte[] salt;
        
        PinData(byte[] pinHash, byte[] salt) {
            this.pinHash = pinHash;
            this.salt = salt;
        }
    }
    
    /**
     * Inner class to hold reset token data.
     */
    private static class ResetTokenData {
        final String token;
        final LocalDateTime expiresAt;
        
        ResetTokenData(String token, LocalDateTime expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }
    }
}