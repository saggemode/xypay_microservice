package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Security services for bank transfers including Night Guard, Large Transaction Shield, and Location Guard.
 * Equivalent to Django's NightGuardService, LargeTransactionShieldService, and LocationGuardService.
 */
@Service
public class SecurityGuardService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityGuardService.class);
    
    @Autowired
    private TransactionShieldService transactionShieldService;
    
    /**
     * Apply Night Guard security check.
     * Equivalent to Django's NightGuardService.apply_night_guard()
     */
    public Map<String, Object> applyNightGuard(BankTransfer transfer) {
        Map<String, Object> result = new HashMap<>();
        
        // TODO: Implement actual night guard logic
        // For now, return not required
        result.put("required", false);
        result.put("reason", "Night guard not implemented yet");
        
        logger.debug("Night Guard check for transfer {}: required={}", 
            transfer.getId(), result.get("required"));
        
        return result;
    }
    
    /**
     * Apply Large Transaction Shield security check.
     * Equivalent to Django's LargeTransactionShieldService.apply_shield()
     */
    public Map<String, Object> applyLargeTransactionShield(BankTransfer transfer) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if user has shield settings
            if (transfer.getUser() == null || transfer.getUser().getId() == null) {
                result.put("required", false);
                result.put("reason", "No user associated with transfer");
                return result;
            }
            
            // Check if shield is required for this amount
            boolean requiresShield = transactionShieldService.requiresShieldVerification(
                transfer.getUser().getId(), transfer.getAmount());
            
            if (requiresShield) {
                // Check if verification has already been passed
                String verificationStatus = getMetadataValue(transfer, "large_tx_shield_status");
                
                if ("verified".equals(verificationStatus)) {
                    result.put("required", false);
                    result.put("reason", "Shield verification already passed");
                } else if ("pending".equals(verificationStatus)) {
                    result.put("required", true);
                    result.put("reason", "Shield verification pending");
                    result.put("status", "pending");
                } else {
                    result.put("required", true);
                    result.put("reason", "Large transaction requires shield verification");
                    result.put("status", "required");
                    result.put("verification_type", "biometric");
                }
            } else {
                result.put("required", false);
                result.put("reason", "Transaction amount below shield threshold");
            }
            
            logger.debug("Large Transaction Shield check for transfer {}: required={}, reason={}", 
                transfer.getId(), result.get("required"), result.get("reason"));
            
        } catch (Exception e) {
            logger.error("Error applying large transaction shield for transfer {}: {}", 
                transfer.getId(), e.getMessage());
            result.put("required", false);
            result.put("reason", "Error checking shield requirements: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Apply Location Guard security check.
     * Equivalent to Django's LocationGuardService.apply_guard()
     */
    public Map<String, Object> applyLocationGuard(BankTransfer transfer) {
        Map<String, Object> result = new HashMap<>();
        
        // TODO: Implement actual location guard logic
        // For now, return not required
        result.put("required", false);
        result.put("reason", "Location guard not implemented yet");
        
        logger.debug("Location Guard check for transfer {}: required={}", 
            transfer.getId(), result.get("required"));
        
        return result;
    }
    
    /**
     * Check if security verification status is passed.
     */
    public boolean isSecurityVerificationPassed(String status) {
        return "face_passed".equals(status) || "fallback_passed".equals(status) || "verified".equals(status);
    }
    
    /**
     * Get metadata value from transfer
     */
    private String getMetadataValue(BankTransfer transfer, String key) {
        String metadata = transfer.getMetadata();
        if (metadata != null && !metadata.isEmpty()) {
            // Simple JSON parsing - in real implementation, use proper JSON library
            String searchPattern = "\"" + key + "\":\"";
            int startIndex = metadata.indexOf(searchPattern);
            if (startIndex != -1) {
                startIndex += searchPattern.length();
                int endIndex = metadata.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return metadata.substring(startIndex, endIndex);
                }
            }
        }
        return null;
    }
}
