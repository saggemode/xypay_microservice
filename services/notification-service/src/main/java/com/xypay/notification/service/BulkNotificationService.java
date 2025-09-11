package com.xypay.notification.service;

import com.xypay.notification.domain.Notification;
import com.xypay.notification.domain.NotificationAnalytics;
import com.xypay.notification.domain.NotificationLevel;
import com.xypay.notification.domain.NotificationType;
import com.xypay.notification.domain.NotificationPreferences;
import com.xypay.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class BulkNotificationService {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationAnalyticsService analyticsService;
    
    @Autowired
    private NotificationPreferencesService preferencesService;
    
    /**
     * Send bulk notifications to multiple users
     */
    @Async
    public CompletableFuture<List<Notification>> sendBulkNotifications(
            List<Long> userIds,
            String title,
            String message,
            NotificationType notificationType,
            NotificationLevel level,
            Map<String, Object> data) {
        
        List<Notification> notifications = new ArrayList<>();
        
        for (Long userId : userIds) {
            try {
                // Check user preferences before sending
                if (isNotificationAllowed(userId, notificationType)) {
                    Notification notification = notificationService.sendNotification(
                        userId, title, message, notificationType, level, data);
                    notifications.add(notification);
                }
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error sending notification to user " + userId + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(notifications);
    }
    
    /**
     * Send bulk notifications with template
     */
    @Async
    public CompletableFuture<List<Notification>> sendBulkNotificationsWithTemplate(
            List<Long> userIds,
            String templateKey,
            Map<String, Object> templateVariables,
            NotificationType notificationType,
            NotificationLevel level) {
        
        List<Notification> notifications = new ArrayList<>();
        
        for (Long userId : userIds) {
            try {
                // Check user preferences before sending
                if (isNotificationAllowed(userId, notificationType)) {
                    // Get user preferences for language
                    String language = preferencesService.getOrCreateUserPreferences(userId).getLanguage();
                    
                    // Process template with variables
                    String title = notificationService.processTemplateSubject(templateKey, language, templateVariables);
                    String message = notificationService.processTemplate(templateKey, language, templateVariables);
                    
                    Notification notification = notificationService.sendNotification(
                        userId, title, message, notificationType, level, templateVariables);
                    notifications.add(notification);
                }
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error sending template notification to user " + userId + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(notifications);
    }
    
    /**
     * Send bulk notifications to users with specific preferences
     */
    @Async
    public CompletableFuture<List<Notification>> sendBulkNotificationsToUsersWithPreferences(
            String channel,
            String notificationType,
            String title,
            String message,
            NotificationType type,
            NotificationLevel level,
            Map<String, Object> data) {
        
        List<Notification> notifications = new ArrayList<>();
        
        // Get users who have the specific channel enabled for the notification type
        List<Long> eligibleUserIds = getEligibleUserIds(channel, notificationType);
        
        for (Long userId : eligibleUserIds) {
            try {
                Notification notification = notificationService.sendNotification(
                    userId, title, message, type, level, data);
                notifications.add(notification);
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error sending notification to user " + userId + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(notifications);
    }
    
    /**
     * Send bulk notifications with batching
     */
    @Async
    public CompletableFuture<List<Notification>> sendBulkNotificationsWithBatching(
            List<Long> userIds,
            String title,
            String message,
            NotificationType notificationType,
            NotificationLevel level,
            Map<String, Object> data,
            int batchSize) {
        
        List<Notification> allNotifications = new ArrayList<>();
        
        // Process users in batches
        for (int i = 0; i < userIds.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, userIds.size());
            List<Long> batch = userIds.subList(i, endIndex);
            
            try {
                List<Notification> batchNotifications = sendBulkNotifications(
                    batch, title, message, notificationType, level, data).get();
                allNotifications.addAll(batchNotifications);
                
                // Add delay between batches to avoid overwhelming the system
                Thread.sleep(1000); // 1 second delay
                
            } catch (Exception e) {
                System.err.println("Error processing batch " + (i / batchSize + 1) + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(allNotifications);
    }
    
    /**
     * Send bulk notifications with rate limiting
     */
    @Async
    public CompletableFuture<List<Notification>> sendBulkNotificationsWithRateLimit(
            List<Long> userIds,
            String title,
            String message,
            NotificationType notificationType,
            NotificationLevel level,
            Map<String, Object> data,
            int notificationsPerSecond) {
        
        List<Notification> notifications = new ArrayList<>();
        long delayMs = 1000 / notificationsPerSecond; // Calculate delay between notifications
        
        for (Long userId : userIds) {
            try {
                // Check user preferences before sending
                if (isNotificationAllowed(userId, notificationType)) {
                    Notification notification = notificationService.sendNotification(
                        userId, title, message, notificationType, level, data);
                    notifications.add(notification);
                    
                    // Rate limiting delay
                    Thread.sleep(delayMs);
                }
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error sending notification to user " + userId + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(notifications);
    }
    
    /**
     * Send bulk notifications with priority handling
     */
    @Async
    public CompletableFuture<List<Notification>> sendBulkNotificationsWithPriority(
            List<Long> userIds,
            String title,
            String message,
            NotificationType notificationType,
            NotificationLevel level,
            Map<String, Object> data,
            int priority) {
        
        List<Notification> notifications = new ArrayList<>();
        
        for (Long userId : userIds) {
            try {
                // Check user preferences before sending
                if (isNotificationAllowed(userId, notificationType)) {
                    Notification notification = notificationService.sendNotification(
                        userId, title, message, notificationType, level, data);
                    
                    // Set priority
                    notification.setPriority(priority);
                    notificationRepository.save(notification);
                    
                    notifications.add(notification);
                }
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error sending priority notification to user " + userId + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(notifications);
    }
    
    /**
     * Send bulk notifications with campaign tracking
     */
    @Async
    public CompletableFuture<List<Notification>> sendBulkNotificationsWithCampaign(
            List<Long> userIds,
            String title,
            String message,
            NotificationType notificationType,
            NotificationLevel level,
            Map<String, Object> data,
            String campaignId,
            String segmentId) {
        
        List<Notification> notifications = new ArrayList<>();
        
        for (Long userId : userIds) {
            try {
                // Check user preferences before sending
                if (isNotificationAllowed(userId, notificationType)) {
                    Notification notification = notificationService.sendNotification(
                        userId, title, message, notificationType, level, data);
                    
                    // Add campaign and segment information to data
                    if (data != null) {
                        data.put("campaign_id", campaignId);
                        data.put("segment_id", segmentId);
                    }
                    
                    notifications.add(notification);
                    
                    // Create analytics record for campaign tracking
                    createCampaignAnalytics(notification.getId(), userId, campaignId, segmentId);
                }
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error sending campaign notification to user " + userId + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(notifications);
    }
    
    /**
     * Check if notification is allowed for user
     */
    private boolean isNotificationAllowed(Long userId, NotificationType notificationType) {
        try {
            // Check if user is in quiet hours
            if (preferencesService.isInQuietHours(userId)) {
                return false;
            }
            
            // Check channel-specific preferences
            return preferencesService.isNotificationAllowed(userId, "EMAIL", notificationType.name()) ||
                   preferencesService.isNotificationAllowed(userId, "SMS", notificationType.name()) ||
                   preferencesService.isNotificationAllowed(userId, "PUSH", notificationType.name()) ||
                   preferencesService.isNotificationAllowed(userId, "IN_APP", notificationType.name());
                   
        } catch (Exception e) {
            // If there's an error checking preferences, allow the notification
            return true;
        }
    }
    
    /**
     * Get eligible user IDs based on preferences
     */
    private List<Long> getEligibleUserIds(String channel, String notificationType) {
        List<Long> eligibleUserIds = new ArrayList<>();
        
        try {
            // Get users who have the channel enabled
            List<NotificationPreferences> usersWithChannelEnabled = getUsersWithChannelEnabled(channel);
            
            for (NotificationPreferences preferences : usersWithChannelEnabled) {
                if (preferences.isNotificationTypeEnabled(channel, notificationType)) {
                    eligibleUserIds.add(preferences.getUserId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting eligible user IDs: " + e.getMessage());
        }
        
        return eligibleUserIds;
    }
    
    /**
     * Get users with specific channel enabled
     */
    private List<NotificationPreferences> getUsersWithChannelEnabled(String channel) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return preferencesService.getUsersWithEmailEnabled();
            case "SMS":
                return preferencesService.getUsersWithSmsEnabled();
            case "PUSH":
                return preferencesService.getUsersWithPushEnabled();
            case "IN_APP":
                return preferencesService.getUsersWithInAppEnabled();
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Create campaign analytics record
     */
    private void createCampaignAnalytics(Long notificationId, Long userId, String campaignId, String segmentId) {
        try {
            // Create analytics record for each channel
            for (NotificationAnalytics.DeliveryChannel channel : NotificationAnalytics.DeliveryChannel.values()) {
                NotificationAnalytics analytics = analyticsService.createAnalyticsRecord(
                    notificationId, userId, channel);
                analytics.setCampaignId(campaignId);
                analytics.setSegmentId(segmentId);
                analyticsService.updateAnalyticsRecord(analytics);
            }
        } catch (Exception e) {
            System.err.println("Error creating campaign analytics: " + e.getMessage());
        }
    }
}
