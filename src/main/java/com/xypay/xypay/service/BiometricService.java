package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for handling biometric authentication.
 * Supports fingerprint, face, and voice biometrics.
 */
@Service
public class BiometricService {
    
    private static final Logger logger = LoggerFactory.getLogger(BiometricService.class);
    
    public enum BiometricType {
        FINGERPRINT("fingerprint"),
        FACE("face"),
        VOICE("voice");
        
        private final String value;
        
        BiometricType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static BiometricType fromString(String value) {
            for (BiometricType type : BiometricType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid biometric type: " + value);
        }
    }
    
    /**
     * Register a new biometric identifier for a user.
     * 
     * @param user User to register biometric for
     * @param biometricType Type of biometric (fingerprint, face, voice)
     * @param biometricData Encrypted/hashed biometric template
     * @return Registration result
     */
    public Map<String, Object> registerBiometric(User user, String biometricType, String biometricData) {
        try {
            // Validate biometric type
            BiometricType.fromString(biometricType);
            
            // In production, integrate with actual biometric hardware/SDK
            // For now, we'll implement a mock version
            
            // Hash the biometric data for secure storage
            String hashedData = hashBiometricData(biometricData);
            
            // In production, store in secure database/HSM
            // For now, store in user profile
            Map<String, Object> biometricDataMap = getUserBiometricData(user);
            if (biometricDataMap == null) {
                biometricDataMap = new HashMap<>();
            }
            
            Map<String, Object> biometricInfo = new HashMap<>();
            biometricInfo.put("template", hashedData);
            biometricInfo.put("registered_at", LocalDateTime.now().toString());
            biometricInfo.put("last_used", null);
            
            biometricDataMap.put(biometricType, biometricInfo);
            storeUserBiometricData(user, biometricDataMap);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", capitalizeFirst(biometricType) + " biometric registered successfully");
            result.put("biometric_type", biometricType);
            result.put("registered_at", biometricInfo.get("registered_at"));
            return result;
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Invalid biometric type. Must be one of: " + 
                Arrays.toString(BiometricType.values()));
            return result;
        } catch (Exception e) {
            logger.error("Error registering biometric: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Verify a biometric against stored template.
     * 
     * @param user User to verify biometric for
     * @param biometricType Type of biometric (fingerprint, face, voice)
     * @param biometricData Biometric data to verify
     * @return Verification result
     */
    public Map<String, Object> verifyBiometric(User user, String biometricType, String biometricData) {
        try {
            BiometricType.fromString(biometricType);
            
            Map<String, Object> biometricDataMap = getUserBiometricData(user);
            if (biometricDataMap == null || !biometricDataMap.containsKey(biometricType)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "No " + biometricType + " biometric registered for user");
                return result;
            }
            
            // Hash the provided biometric data
            String hashedData = hashBiometricData(biometricData);
            
            // Compare with stored template
            @SuppressWarnings("unchecked")
            Map<String, Object> biometricInfo = (Map<String, Object>) biometricDataMap.get(biometricType);
            String storedTemplate = (String) biometricInfo.get("template");
            boolean isMatch = hashedData.equals(storedTemplate);
            
            if (isMatch) {
                // Update last used timestamp
                biometricInfo.put("last_used", LocalDateTime.now().toString());
                storeUserBiometricData(user, biometricDataMap);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", capitalizeFirst(biometricType) + " verification successful");
                result.put("biometric_type", biometricType);
                result.put("verified_at", LocalDateTime.now().toString());
                return result;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", capitalizeFirst(biometricType) + " verification failed");
                return result;
            }
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Invalid biometric type. Must be one of: " + 
                Arrays.toString(BiometricType.values()));
            return result;
        } catch (Exception e) {
            logger.error("Error verifying biometric: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Get list of registered biometrics for a user.
     * 
     * @param user User to get biometrics for
     * @return List of registered biometrics and their status
     */
    public Map<String, Object> getRegisteredBiometrics(User user) {
        try {
            Map<String, Object> biometricDataMap = getUserBiometricData(user);
            if (biometricDataMap == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("registered_biometrics", new ArrayList<>());
                result.put("total_registered", 0);
                return result;
            }
            
            List<Map<String, Object>> registered = new ArrayList<>();
            for (Map.Entry<String, Object> entry : biometricDataMap.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> biometricInfo = (Map<String, Object>) entry.getValue();
                Map<String, Object> biometric = new HashMap<>();
                biometric.put("type", entry.getKey());
                biometric.put("registered_at", biometricInfo.get("registered_at"));
                biometric.put("last_used", biometricInfo.get("last_used"));
                registered.add(biometric);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("registered_biometrics", registered);
            result.put("total_registered", registered.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting registered biometrics: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("registered_biometrics", new ArrayList<>());
            result.put("total_registered", 0);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Remove a biometric from a user's account.
     * 
     * @param user User to remove biometric from
     * @param biometricType Type of biometric to remove
     * @return Removal result
     */
    public Map<String, Object> removeBiometric(User user, String biometricType) {
        try {
            BiometricType.fromString(biometricType);
            
            Map<String, Object> biometricDataMap = getUserBiometricData(user);
            if (biometricDataMap == null || !biometricDataMap.containsKey(biometricType)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "No " + biometricType + " biometric registered for user");
                return result;
            }
            
            biometricDataMap.remove(biometricType);
            storeUserBiometricData(user, biometricDataMap);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", capitalizeFirst(biometricType) + " biometric removed successfully");
            return result;
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Invalid biometric type. Must be one of: " + 
                Arrays.toString(BiometricType.values()));
            return result;
        } catch (Exception e) {
            logger.error("Error removing biometric: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Check if a user has any biometrics registered.
     * 
     * @param user User to check
     * @return True if user has at least one biometric registered
     */
    public boolean hasAnyBiometric(User user) {
        try {
            Map<String, Object> biometricDataMap = getUserBiometricData(user);
            return biometricDataMap != null && !biometricDataMap.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking biometrics: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a user has a specific biometric type registered.
     * 
     * @param user User to check
     * @param biometricType Type of biometric to check
     * @return True if user has the specified biometric registered
     */
    public boolean hasBiometric(User user, String biometricType) {
        try {
            Map<String, Object> biometricDataMap = getUserBiometricData(user);
            return biometricDataMap != null && biometricDataMap.containsKey(biometricType);
        } catch (Exception e) {
            logger.error("Error checking biometric: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Hash biometric data for secure storage.
     * 
     * @param biometricData Raw biometric data
     * @return Hashed biometric data
     */
    private String hashBiometricData(String biometricData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(biometricData.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * Get user biometric data from storage.
     * Note: This is a placeholder - you'll need to implement actual retrieval
     */
    private Map<String, Object> getUserBiometricData(User user) {
        // TODO: Implement actual retrieval from your User entity or UserProfile
        // For now, return null as placeholder
        return null;
    }
    
    /**
     * Store user biometric data.
     * Note: This is a placeholder - you'll need to implement actual storage
     */
    private void storeUserBiometricData(User user, Map<String, Object> biometricData) {
        // TODO: Implement actual storage in your User entity or UserProfile
        logger.debug("Storing biometric data for user {}", user.getId());
    }
    
    /**
     * Capitalize first letter of a string.
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
