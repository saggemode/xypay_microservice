package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserPreferences;
import com.xypay.xypay.repository.UserPreferencesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserPreferencesService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserPreferencesService.class);
    
    @Autowired
    private UserPreferencesRepository userPreferencesRepository;
    
    /**
     * Get user preferences by user ID
     */
    public UserPreferences getUserPreferences(UUID userId) {
        try {
            User user = new User();
            user.setId(userId);
            Optional<UserPreferences> preferencesOpt = userPreferencesRepository.findByUser(user);
            if (preferencesOpt.isPresent()) {
                return preferencesOpt.get();
            } else {
                // Create default preferences if none exist
                return createDefaultPreferences(userId);
            }
        } catch (Exception e) {
            logger.error("Error getting user preferences for user {}: {}", userId, e.getMessage(), e);
            return createDefaultPreferences(userId);
        }
    }
    
    /**
     * Get user preferences by user object
     */
    public UserPreferences getUserPreferences(User user) {
        return getUserPreferences(user.getId());
    }
    
    /**
     * Create default preferences for a user
     */
    public UserPreferences createDefaultPreferences(UUID userId) {
        try {
            User user = new User();
            user.setId(userId);
            
            UserPreferences preferences = new UserPreferences();
            preferences.setUser(user);
            preferences.setCreatedAt(LocalDateTime.now());
            preferences.setUpdatedAt(LocalDateTime.now());
            
            UserPreferences savedPreferences = userPreferencesRepository.save(preferences);
            logger.info("Created default preferences for user {}", userId);
            return savedPreferences;
            
        } catch (Exception e) {
            logger.error("Error creating default preferences for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to create user preferences: " + e.getMessage());
        }
    }
    
    /**
     * Update Spend and Save preferences
     */
    public UserPreferences updateSpendSavePreferences(UUID userId, BigDecimal savingsPercentage, 
                                                     BigDecimal minTransactionAmount, String fundingSource,
                                                     Boolean autoWithdrawalEnabled, BigDecimal autoWithdrawalThreshold,
                                                     String withdrawalDestination) {
        try {
            UserPreferences preferences = getUserPreferences(userId);
            
            // Validate inputs
            validateSpendSaveSettings(savingsPercentage, minTransactionAmount, autoWithdrawalThreshold);
            
            preferences.setSavingsPercentage(savingsPercentage);
            preferences.setMinTransactionAmount(minTransactionAmount);
            preferences.setFundingSource(fundingSource);
            preferences.setAutoWithdrawalEnabled(autoWithdrawalEnabled);
            preferences.setAutoWithdrawalThreshold(autoWithdrawalThreshold);
            preferences.setWithdrawalDestination(withdrawalDestination);
            preferences.setUpdatedAt(LocalDateTime.now());
            
            UserPreferences savedPreferences = userPreferencesRepository.save(preferences);
            logger.info("Updated Spend and Save preferences for user {}", userId);
            return savedPreferences;
            
        } catch (Exception e) {
            logger.error("Error updating Spend and Save preferences for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update Spend and Save preferences: " + e.getMessage());
        }
    }
    
    /**
     * Update notification preferences
     */
    public UserPreferences updateNotificationPreferences(UUID userId, Boolean emailNotifications,
                                                        Boolean smsNotifications, Boolean pushNotifications,
                                                        Boolean spendSaveNotifications, Boolean interestNotifications,
                                                        Boolean milestoneNotifications) {
        try {
            UserPreferences preferences = getUserPreferences(userId);
            
            preferences.setEmailNotifications(emailNotifications);
            preferences.setSmsNotifications(smsNotifications);
            preferences.setPushNotifications(pushNotifications);
            preferences.setSpendSaveNotifications(spendSaveNotifications);
            preferences.setInterestNotifications(interestNotifications);
            preferences.setMilestoneNotifications(milestoneNotifications);
            preferences.setUpdatedAt(LocalDateTime.now());
            
            UserPreferences savedPreferences = userPreferencesRepository.save(preferences);
            logger.info("Updated notification preferences for user {}", userId);
            return savedPreferences;
            
        } catch (Exception e) {
            logger.error("Error updating notification preferences for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update notification preferences: " + e.getMessage());
        }
    }
    
    /**
     * Enable/disable Spend and Save
     */
    public UserPreferences toggleSpendSave(UUID userId, Boolean enabled) {
        try {
            UserPreferences preferences = getUserPreferences(userId);
            preferences.setSpendSaveEnabled(enabled);
            preferences.setUpdatedAt(LocalDateTime.now());
            
            UserPreferences savedPreferences = userPreferencesRepository.save(preferences);
            logger.info("Toggled Spend and Save for user {} to {}", userId, enabled);
            return savedPreferences;
            
        } catch (Exception e) {
            logger.error("Error toggling Spend and Save for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to toggle Spend and Save: " + e.getMessage());
        }
    }
    
    /**
     * Get preferences as a map for easy JSON serialization
     */
    public Map<String, Object> getPreferencesAsMap(UUID userId) {
        try {
            UserPreferences preferences = getUserPreferences(userId);
            Map<String, Object> prefsMap = new HashMap<>();
            
            // Spend and Save preferences
            prefsMap.put("spendSaveEnabled", preferences.getSpendSaveEnabled());
            prefsMap.put("savingsPercentage", preferences.getSavingsPercentage());
            prefsMap.put("minTransactionAmount", preferences.getMinTransactionAmount());
            prefsMap.put("fundingSource", preferences.getFundingSource());
            prefsMap.put("autoWithdrawalEnabled", preferences.getAutoWithdrawalEnabled());
            prefsMap.put("autoWithdrawalThreshold", preferences.getAutoWithdrawalThreshold());
            prefsMap.put("withdrawalDestination", preferences.getWithdrawalDestination());
            
            // Notification preferences
            prefsMap.put("emailNotifications", preferences.getEmailNotifications());
            prefsMap.put("smsNotifications", preferences.getSmsNotifications());
            prefsMap.put("pushNotifications", preferences.getPushNotifications());
            prefsMap.put("spendSaveNotifications", preferences.getSpendSaveNotifications());
            prefsMap.put("interestNotifications", preferences.getInterestNotifications());
            prefsMap.put("milestoneNotifications", preferences.getMilestoneNotifications());
            
            // Timestamps
            prefsMap.put("lastSettingsUpdate", preferences.getLastSettingsUpdate());
            prefsMap.put("createdAt", preferences.getCreatedAt());
            prefsMap.put("updatedAt", preferences.getUpdatedAt());
            
            return prefsMap;
            
        } catch (Exception e) {
            logger.error("Error getting preferences as map for user {}: {}", userId, e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    // Validation methods
    private void validateSpendSaveSettings(BigDecimal savingsPercentage, BigDecimal minTransactionAmount, 
                                         BigDecimal autoWithdrawalThreshold) {
        if (savingsPercentage != null && (savingsPercentage.compareTo(BigDecimal.ZERO) <= 0 || 
            savingsPercentage.compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("Savings percentage must be between 0.01 and 100");
        }
        
        if (minTransactionAmount != null && minTransactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Minimum transaction amount must be greater than 0");
        }
        
        if (autoWithdrawalThreshold != null && autoWithdrawalThreshold.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Auto-withdrawal threshold must be greater than 0");
        }
    }
}