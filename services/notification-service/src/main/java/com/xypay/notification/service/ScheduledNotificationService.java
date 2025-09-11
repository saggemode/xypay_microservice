package com.xypay.notification.service;

import com.xypay.notification.domain.NotificationType;
import com.xypay.notification.domain.ScheduledNotification;
import com.xypay.notification.repository.ScheduledNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ScheduledNotificationService {
    
    @Autowired
    private ScheduledNotificationRepository scheduledNotificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Schedule a notification for later delivery
     */
    public ScheduledNotification scheduleNotification(Long userId, String title, String message, 
                                                    NotificationType notificationType, LocalDateTime scheduledFor) {
        ScheduledNotification scheduledNotification = new ScheduledNotification(
            userId, title, message, notificationType, scheduledFor);
        
        scheduledNotification.setStatus(ScheduledNotification.ScheduleStatus.SCHEDULED);
        return scheduledNotificationRepository.save(scheduledNotification);
    }
    
    /**
     * Schedule a delayed notification
     */
    public ScheduledNotification scheduleDelayedNotification(Long userId, String title, String message, 
                                                           NotificationType notificationType, int delayMinutes) {
        LocalDateTime scheduledFor = LocalDateTime.now().plusMinutes(delayMinutes);
        return scheduleNotification(userId, title, message, notificationType, scheduledFor);
    }
    
    /**
     * Schedule a recurring notification
     */
    public ScheduledNotification scheduleRecurringNotification(Long userId, String title, String message, 
                                                             NotificationType notificationType, String cronExpression) {
        ScheduledNotification scheduledNotification = new ScheduledNotification(
            userId, title, message, notificationType, LocalDateTime.now());
        
        scheduledNotification.setScheduleType(ScheduledNotification.ScheduleType.RECURRING);
        scheduledNotification.setRecurrencePattern(cronExpression);
        scheduledNotification.setStatus(ScheduledNotification.ScheduleStatus.SCHEDULED);
        
        return scheduledNotificationRepository.save(scheduledNotification);
    }
    
    /**
     * Schedule a conditional notification
     */
    public ScheduledNotification scheduleConditionalNotification(Long userId, String title, String message, 
                                                               NotificationType notificationType, String conditionExpression) {
        ScheduledNotification scheduledNotification = new ScheduledNotification(
            userId, title, message, notificationType, LocalDateTime.now());
        
        scheduledNotification.setScheduleType(ScheduledNotification.ScheduleType.CONDITIONAL);
        scheduledNotification.setConditionExpression(conditionExpression);
        scheduledNotification.setStatus(ScheduledNotification.ScheduleStatus.PENDING);
        
        return scheduledNotificationRepository.save(scheduledNotification);
    }
    
    /**
     * Get scheduled notification by ID
     */
    public Optional<ScheduledNotification> getScheduledNotification(Long id) {
        return scheduledNotificationRepository.findById(id);
    }
    
    /**
     * Get user's scheduled notifications
     */
    public List<ScheduledNotification> getUserScheduledNotifications(Long userId) {
        return scheduledNotificationRepository.findPendingAndScheduledByUserId(userId);
    }
    
    /**
     * Cancel a scheduled notification
     */
    public void cancelScheduledNotification(Long id) {
        Optional<ScheduledNotification> notificationOpt = scheduledNotificationRepository.findById(id);
        if (notificationOpt.isPresent()) {
            ScheduledNotification notification = notificationOpt.get();
            notification.cancel();
            scheduledNotificationRepository.save(notification);
        }
    }
    
    /**
     * Update scheduled notification
     */
    public ScheduledNotification updateScheduledNotification(ScheduledNotification notification) {
        return scheduledNotificationRepository.save(notification);
    }
    
    /**
     * Process ready-to-send notifications (called by scheduler)
     */
    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    @Async
    public void processReadyNotifications() {
        List<ScheduledNotification> readyNotifications = scheduledNotificationRepository.findReadyToSend(LocalDateTime.now());
        
        for (ScheduledNotification scheduledNotification : readyNotifications) {
            try {
                // Send the notification
                notificationService.sendNotification(
                    scheduledNotification.getUserId(),
                    scheduledNotification.getTitle(),
                    scheduledNotification.getMessage(),
                    scheduledNotification.getNotificationType(),
                    null, // level
                    null  // data
                );
                
                // Mark as sent
                scheduledNotification.markAsSent();
                scheduledNotificationRepository.save(scheduledNotification);
                
            } catch (Exception e) {
                // Mark as failed and schedule retry
                scheduledNotification.markAsFailed(e.getMessage());
                scheduledNotificationRepository.save(scheduledNotification);
            }
        }
    }
    
    /**
     * Process failed notifications for retry (called by scheduler)
     */
    @Scheduled(fixedDelay = 60000) // Run every minute
    @Async
    public void processFailedNotifications() {
        List<ScheduledNotification> failedNotifications = scheduledNotificationRepository.findReadyToRetry(LocalDateTime.now());
        
        for (ScheduledNotification scheduledNotification : failedNotifications) {
            if (scheduledNotification.canRetry()) {
                scheduledNotification.scheduleRetry();
                scheduledNotificationRepository.save(scheduledNotification);
            }
        }
    }
    
    /**
     * Clean up overdue notifications (called by scheduler)
     */
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Async
    public void cleanupOverdueNotifications() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
        List<ScheduledNotification> overdueNotifications = scheduledNotificationRepository.findOverdueNotifications(cutoffTime);
        
        for (ScheduledNotification scheduledNotification : overdueNotifications) {
            scheduledNotification.markAsFailed("Notification overdue");
            scheduledNotificationRepository.save(scheduledNotification);
        }
    }
    
    /**
     * Get notification statistics
     */
    public Map<String, Object> getScheduledNotificationStatistics() {
        Map<String, Object> stats = Map.of(
            "totalScheduled", scheduledNotificationRepository.count(),
            "pending", scheduledNotificationRepository.countPendingNotifications(),
            "scheduled", scheduledNotificationRepository.countScheduledNotifications(),
            "sent", scheduledNotificationRepository.countSentNotifications(),
            "failed", scheduledNotificationRepository.countFailedNotifications()
        );
        
        return stats;
    }
    
    /**
     * Get user's scheduled notification count
     */
    public Long getUserScheduledNotificationCount(Long userId) {
        return scheduledNotificationRepository.countPendingAndScheduledByUserId(userId);
    }
    
    /**
     * Get notifications by date range
     */
    public List<ScheduledNotification> getNotificationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduledNotificationRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Get sent notifications by date range
     */
    public List<ScheduledNotification> getSentNotificationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduledNotificationRepository.findSentByDateRange(startDate, endDate);
    }
    
    /**
     * Get high priority notifications
     */
    public List<ScheduledNotification> getHighPriorityNotifications(Integer minPriority) {
        return scheduledNotificationRepository.findByMinPriority(minPriority);
    }
    
    /**
     * Delete scheduled notification
     */
    public void deleteScheduledNotification(Long id) {
        scheduledNotificationRepository.deleteById(id);
    }
    
    /**
     * Bulk cancel notifications for user
     */
    public void bulkCancelUserNotifications(Long userId) {
        List<ScheduledNotification> userNotifications = scheduledNotificationRepository.findPendingAndScheduledByUserId(userId);
        
        for (ScheduledNotification notification : userNotifications) {
            notification.cancel();
        }
        
        scheduledNotificationRepository.saveAll(userNotifications);
    }
}
