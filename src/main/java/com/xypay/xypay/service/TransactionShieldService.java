package com.xypay.xypay.service;

import com.xypay.xypay.domain.LargeTransactionShieldSettings;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.LargeTransactionShieldSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionShieldService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionShieldService.class);
    
    @Autowired
    private LargeTransactionShieldSettingsRepository shieldRepository;
    
    /**
     * Create or update shield settings for a user
     */
    @Transactional
    public LargeTransactionShieldSettings saveShieldSettings(UUID userId, BigDecimal perTransactionLimit, 
                                                           BigDecimal dailyLimit, BigDecimal monthlyLimit, 
                                                           boolean enableFaceRecognition, String faceTemplateHash) {
        User user = new User();
        user.setId(userId);
        
        Optional<LargeTransactionShieldSettings> existingSettings = shieldRepository.findByUser(user);
        LargeTransactionShieldSettings settings;
        
        if (existingSettings.isPresent()) {
            settings = existingSettings.get();
            logger.info("Updating existing shield settings for user {}", userId);
        } else {
            settings = new LargeTransactionShieldSettings(user);
            logger.info("Creating new shield settings for user {}", userId);
        }
        
        // Update settings
        settings.setEnabled(true);
        settings.setPerTransactionLimit(perTransactionLimit);
        settings.setDailyLimit(dailyLimit);
        settings.setMonthlyLimit(monthlyLimit);
        
        if (enableFaceRecognition && faceTemplateHash != null && !faceTemplateHash.trim().isEmpty()) {
            settings.registerFace(faceTemplateHash);
        }
        
        LargeTransactionShieldSettings savedSettings = shieldRepository.save(settings);
        logger.info("Shield settings saved for user {}: enabled={}, perTxLimit={}", 
            userId, savedSettings.getEnabled(), savedSettings.getPerTransactionLimit());
        
        return savedSettings;
    }
    
    /**
     * Get shield settings for a user
     */
    public Optional<LargeTransactionShieldSettings> getShieldSettings(UUID userId) {
        User user = new User();
        user.setId(userId);
        return shieldRepository.findByUser(user);
    }
    
    /**
     * Get all shield settings with pagination
     */
    public Page<LargeTransactionShieldSettings> getAllShieldSettings(Pageable pageable) {
        return shieldRepository.findAll(pageable);
    }
    
    /**
     * Enable shield for a user
     */
    @Transactional
    public LargeTransactionShieldSettings enableShield(UUID userId, BigDecimal perTransactionLimit, 
                                                     BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        User user = new User();
        user.setId(userId);
        
        Optional<LargeTransactionShieldSettings> existingSettings = shieldRepository.findByUser(user);
        LargeTransactionShieldSettings settings;
        
        if (existingSettings.isPresent()) {
            settings = existingSettings.get();
        } else {
            settings = new LargeTransactionShieldSettings(user);
        }
        
        settings.enableShield(perTransactionLimit, dailyLimit, monthlyLimit);
        return shieldRepository.save(settings);
    }
    
    /**
     * Disable shield for a user
     */
    @Transactional
    public LargeTransactionShieldSettings disableShield(UUID userId) {
        User user = new User();
        user.setId(userId);
        
        Optional<LargeTransactionShieldSettings> existingSettings = shieldRepository.findByUser(user);
        if (existingSettings.isPresent()) {
            LargeTransactionShieldSettings settings = existingSettings.get();
            settings.disableShield();
            return shieldRepository.save(settings);
        }
        
        return null;
    }
    
    /**
     * Check if a transaction requires shield verification
     */
    public boolean requiresShieldVerification(UUID userId, BigDecimal amount) {
        User user = new User();
        user.setId(userId);
        
        Optional<LargeTransactionShieldSettings> settings = shieldRepository.findByUser(user);
        if (settings.isPresent()) {
            return settings.get().requiresVerification(amount);
        }
        
        return false;
    }
    
    /**
     * Get shield statistics
     */
    public Map<String, Object> getShieldStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Active shields count
        long activeShields = shieldRepository.countByEnabledTrue();
        stats.put("activeShields", activeShields);
        
        // Biometric verifications (mock data for now)
        stats.put("biometricVerifications", 1247);
        
        // Blocked transactions today (mock data for now)
        long blockedToday = 0;
        stats.put("blockedToday", blockedToday);
        
        // Approved transactions today (mock data for now)
        long approvedToday = 0;
        stats.put("approvedToday", approvedToday);
        
        // Total shields registered
        long totalShields = shieldRepository.count();
        stats.put("totalShields", totalShields);
        
        // Shields with face recognition
        long faceRecognitionShields = shieldRepository.findShieldsWithFaceRecognition().size();
        stats.put("faceRecognitionShields", faceRecognitionShields);
        
        logger.info("Shield statistics calculated: active={}, blocked={}, approved={}", 
            activeShields, blockedToday, approvedToday);
        
        return stats;
    }
    
    /**
     * Get recent shield activity
     */
    public List<Map<String, Object>> getRecentShieldActivity(int limit) {
        // This would typically query a shield activity log table
        // For now, return mock data based on recent transactions
        List<Map<String, Object>> activity = new java.util.ArrayList<>();
        
        // Mock recent activity data
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("time", LocalDateTime.now().minusHours(1).toString());
        activity1.put("account", "ACC-001234");
        activity1.put("transaction", "Transfer");
        activity1.put("amount", "₦75,000");
        activity1.put("verification", "Biometric");
        activity1.put("status", "Approved");
        activity.add(activity1);
        
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("time", LocalDateTime.now().minusHours(2).toString());
        activity2.put("account", "ACC-001235");
        activity2.put("transaction", "Withdrawal");
        activity2.put("amount", "₦120,000");
        activity2.put("verification", "Manager");
        activity2.put("status", "Pending");
        activity.add(activity2);
        
        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("time", LocalDateTime.now().minusHours(3).toString());
        activity3.put("account", "ACC-001236");
        activity3.put("transaction", "Transfer");
        activity3.put("amount", "₦200,000");
        activity3.put("verification", "Failed");
        activity3.put("status", "Blocked");
        activity.add(activity3);
        
        return activity.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Update shield limits
     */
    @Transactional
    public LargeTransactionShieldSettings updateShieldLimits(UUID userId, BigDecimal perTransactionLimit, 
                                                           BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        User user = new User();
        user.setId(userId);
        
        Optional<LargeTransactionShieldSettings> existingSettings = shieldRepository.findByUser(user);
        if (existingSettings.isPresent()) {
            LargeTransactionShieldSettings settings = existingSettings.get();
            settings.setPerTransactionLimit(perTransactionLimit);
            settings.setDailyLimit(dailyLimit);
            settings.setMonthlyLimit(monthlyLimit);
            return shieldRepository.save(settings);
        }
        
        return null;
    }
    
    /**
     * Update biometric settings
     */
    @Transactional
    public LargeTransactionShieldSettings updateBiometricSettings(UUID userId, boolean enableFingerprint, 
                                                                boolean enableFaceRecognition, boolean enableVoiceRecognition, 
                                                                boolean enablePinVerification, BigDecimal biometricThreshold) {
        User user = new User();
        user.setId(userId);
        
        Optional<LargeTransactionShieldSettings> existingSettings = shieldRepository.findByUser(user);
        if (existingSettings.isPresent()) {
            LargeTransactionShieldSettings settings = existingSettings.get();
            
            // Update biometric settings (stored in metadata or separate fields)
            // For now, we'll use the existing face recognition field
            if (enableFaceRecognition) {
                // In a real implementation, you'd store these settings properly
                logger.info("Face recognition enabled for user {}", userId);
            }
            
            return shieldRepository.save(settings);
        }
        
        return null;
    }
    
    /**
     * Delete shield settings
     */
    @Transactional
    public void deleteShieldSettings(UUID userId) {
        User user = new User();
        user.setId(userId);
        
        Optional<LargeTransactionShieldSettings> settings = shieldRepository.findByUser(user);
        if (settings.isPresent()) {
            shieldRepository.delete(settings.get());
            logger.info("Shield settings deleted for user {}", userId);
        }
    }
    
    /**
     * Get shields requiring verification for amount
     */
    public List<LargeTransactionShieldSettings> getShieldsRequiringVerification(BigDecimal amount) {
        return shieldRepository.findShieldsRequiringVerification(amount);
    }
}
