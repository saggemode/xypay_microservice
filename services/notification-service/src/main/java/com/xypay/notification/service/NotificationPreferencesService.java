package com.xypay.notification.service;

import com.xypay.notification.domain.NotificationPreferences;
import com.xypay.notification.repository.NotificationPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationPreferencesService {
    
    @Autowired
    private NotificationPreferencesRepository preferencesRepository;
    
    /**
     * Create default notification preferences for user
     */
    public NotificationPreferences createDefaultPreferences(Long userId) {
        NotificationPreferences preferences = new NotificationPreferences(userId);
        return preferencesRepository.save(preferences);
    }
    
    /**
     * Get user notification preferences
     */
    public Optional<NotificationPreferences> getUserPreferences(Long userId) {
        return preferencesRepository.findByUserId(userId);
    }
    
    /**
     * Get or create user notification preferences
     */
    public NotificationPreferences getOrCreateUserPreferences(Long userId) {
        Optional<NotificationPreferences> preferencesOpt = preferencesRepository.findByUserId(userId);
        
        if (preferencesOpt.isPresent()) {
            return preferencesOpt.get();
        } else {
            return createDefaultPreferences(userId);
        }
    }
    
    /**
     * Update user notification preferences
     */
    public NotificationPreferences updatePreferences(Long userId, NotificationPreferences updatedPreferences) {
        NotificationPreferences preferences = getOrCreateUserPreferences(userId);
        
        // Update email preferences
        if (updatedPreferences.getEmailEnabled() != null) {
            preferences.setEmailEnabled(updatedPreferences.getEmailEnabled());
        }
        if (updatedPreferences.getEmailTransactions() != null) {
            preferences.setEmailTransactions(updatedPreferences.getEmailTransactions());
        }
        if (updatedPreferences.getEmailSecurity() != null) {
            preferences.setEmailSecurity(updatedPreferences.getEmailSecurity());
        }
        if (updatedPreferences.getEmailMarketing() != null) {
            preferences.setEmailMarketing(updatedPreferences.getEmailMarketing());
        }
        if (updatedPreferences.getEmailSupport() != null) {
            preferences.setEmailSupport(updatedPreferences.getEmailSupport());
        }
        if (updatedPreferences.getEmailSavings() != null) {
            preferences.setEmailSavings(updatedPreferences.getEmailSavings());
        }
        
        // Update SMS preferences
        if (updatedPreferences.getSmsEnabled() != null) {
            preferences.setSmsEnabled(updatedPreferences.getSmsEnabled());
        }
        if (updatedPreferences.getSmsTransactions() != null) {
            preferences.setSmsTransactions(updatedPreferences.getSmsTransactions());
        }
        if (updatedPreferences.getSmsSecurity() != null) {
            preferences.setSmsSecurity(updatedPreferences.getSmsSecurity());
        }
        if (updatedPreferences.getSmsMarketing() != null) {
            preferences.setSmsMarketing(updatedPreferences.getSmsMarketing());
        }
        if (updatedPreferences.getSmsSupport() != null) {
            preferences.setSmsSupport(updatedPreferences.getSmsSupport());
        }
        if (updatedPreferences.getSmsSavings() != null) {
            preferences.setSmsSavings(updatedPreferences.getSmsSavings());
        }
        
        // Update push preferences
        if (updatedPreferences.getPushEnabled() != null) {
            preferences.setPushEnabled(updatedPreferences.getPushEnabled());
        }
        if (updatedPreferences.getPushTransactions() != null) {
            preferences.setPushTransactions(updatedPreferences.getPushTransactions());
        }
        if (updatedPreferences.getPushSecurity() != null) {
            preferences.setPushSecurity(updatedPreferences.getPushSecurity());
        }
        if (updatedPreferences.getPushMarketing() != null) {
            preferences.setPushMarketing(updatedPreferences.getPushMarketing());
        }
        if (updatedPreferences.getPushSupport() != null) {
            preferences.setPushSupport(updatedPreferences.getPushSupport());
        }
        if (updatedPreferences.getPushSavings() != null) {
            preferences.setPushSavings(updatedPreferences.getPushSavings());
        }
        
        // Update in-app preferences
        if (updatedPreferences.getInAppEnabled() != null) {
            preferences.setInAppEnabled(updatedPreferences.getInAppEnabled());
        }
        if (updatedPreferences.getInAppTransactions() != null) {
            preferences.setInAppTransactions(updatedPreferences.getInAppTransactions());
        }
        if (updatedPreferences.getInAppSecurity() != null) {
            preferences.setInAppSecurity(updatedPreferences.getInAppSecurity());
        }
        if (updatedPreferences.getInAppMarketing() != null) {
            preferences.setInAppMarketing(updatedPreferences.getInAppMarketing());
        }
        if (updatedPreferences.getInAppSupport() != null) {
            preferences.setInAppSupport(updatedPreferences.getInAppSupport());
        }
        if (updatedPreferences.getInAppSavings() != null) {
            preferences.setInAppSavings(updatedPreferences.getInAppSavings());
        }
        
        // Update other preferences
        if (updatedPreferences.getDigestFrequency() != null) {
            preferences.setDigestFrequency(updatedPreferences.getDigestFrequency());
        }
        if (updatedPreferences.getQuietHoursStart() != null) {
            preferences.setQuietHoursStart(updatedPreferences.getQuietHoursStart());
        }
        if (updatedPreferences.getQuietHoursEnd() != null) {
            preferences.setQuietHoursEnd(updatedPreferences.getQuietHoursEnd());
        }
        if (updatedPreferences.getTimezone() != null) {
            preferences.setTimezone(updatedPreferences.getTimezone());
        }
        if (updatedPreferences.getLanguage() != null) {
            preferences.setLanguage(updatedPreferences.getLanguage());
        }
        
        return preferencesRepository.save(preferences);
    }
    
    /**
     * Check if user allows notification type on channel
     */
    public boolean isNotificationAllowed(Long userId, String channel, String notificationType) {
        NotificationPreferences preferences = getOrCreateUserPreferences(userId);
        return preferences.isNotificationTypeEnabled(channel, notificationType);
    }
    
    /**
     * Check if user is in quiet hours
     */
    public boolean isInQuietHours(Long userId) {
        NotificationPreferences preferences = getOrCreateUserPreferences(userId);
        return preferences.isInQuietHours();
    }
    
    /**
     * Get users who have email enabled
     */
    public List<NotificationPreferences> getUsersWithEmailEnabled() {
        return preferencesRepository.findUsersWithEmailEnabled();
    }
    
    /**
     * Get users who have SMS enabled
     */
    public List<NotificationPreferences> getUsersWithSmsEnabled() {
        return preferencesRepository.findUsersWithSmsEnabled();
    }
    
    /**
     * Get users who have push enabled
     */
    public List<NotificationPreferences> getUsersWithPushEnabled() {
        return preferencesRepository.findUsersWithPushEnabled();
    }
    
    /**
     * Get users who have in-app enabled
     */
    public List<NotificationPreferences> getUsersWithInAppEnabled() {
        return preferencesRepository.findUsersWithInAppEnabled();
    }
    
    /**
     * Get users who have marketing enabled for channel
     */
    public List<NotificationPreferences> getUsersWithMarketingEnabled(String channel) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return preferencesRepository.findUsersWithEmailMarketingEnabled();
            case "SMS":
                return preferencesRepository.findUsersWithSmsMarketingEnabled();
            case "PUSH":
                return preferencesRepository.findUsersWithPushMarketingEnabled();
            case "IN_APP":
                return preferencesRepository.findUsersWithInAppMarketingEnabled();
            default:
                return List.of();
        }
    }
    
    /**
     * Get users by digest frequency
     */
    public List<NotificationPreferences> getUsersByDigestFrequency(String frequency) {
        return preferencesRepository.findByDigestFrequency(frequency);
    }
    
    /**
     * Get users by language
     */
    public List<NotificationPreferences> getUsersByLanguage(String language) {
        return preferencesRepository.findByLanguage(language);
    }
    
    /**
     * Get users by timezone
     */
    public List<NotificationPreferences> getUsersByTimezone(String timezone) {
        return preferencesRepository.findByTimezone(timezone);
    }
    
    /**
     * Get notification preferences statistics
     */
    public Map<String, Object> getPreferencesStatistics() {
        Map<String, Object> stats = Map.of(
            "totalUsers", preferencesRepository.count(),
            "emailEnabled", preferencesRepository.countUsersWithEmailEnabled(),
            "smsEnabled", preferencesRepository.countUsersWithSmsEnabled(),
            "pushEnabled", preferencesRepository.countUsersWithPushEnabled(),
            "inAppEnabled", preferencesRepository.countUsersWithInAppEnabled(),
            "emailMarketing", preferencesRepository.countUsersWithEmailMarketingEnabled(),
            "smsMarketing", preferencesRepository.countUsersWithSmsMarketingEnabled(),
            "pushMarketing", preferencesRepository.countUsersWithPushMarketingEnabled(),
            "inAppMarketing", preferencesRepository.countUsersWithInAppMarketingEnabled()
        );
        
        return stats;
    }
    
    /**
     * Bulk update preferences for multiple users
     */
    public void bulkUpdatePreferences(List<Long> userIds, NotificationPreferences templatePreferences) {
        for (Long userId : userIds) {
            updatePreferences(userId, templatePreferences);
        }
    }
    
    /**
     * Reset user preferences to default
     */
    public NotificationPreferences resetToDefault(Long userId) {
        NotificationPreferences defaultPreferences = new NotificationPreferences(userId);
        return preferencesRepository.save(defaultPreferences);
    }
    
    /**
     * Delete user preferences
     */
    public void deleteUserPreferences(Long userId) {
        Optional<NotificationPreferences> preferencesOpt = preferencesRepository.findByUserId(userId);
        if (preferencesOpt.isPresent()) {
            preferencesRepository.delete(preferencesOpt.get());
        }
    }
}
