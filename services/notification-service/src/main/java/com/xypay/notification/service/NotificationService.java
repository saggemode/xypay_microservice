package com.xypay.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xypay.notification.domain.Notification;
import com.xypay.notification.domain.NotificationLevel;
import com.xypay.notification.domain.NotificationStatus;
import com.xypay.notification.domain.NotificationType;
import com.xypay.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private SMSNotificationService smsNotificationService;
    
    @Autowired
    private PushNotificationService pushNotificationService;
    
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;
    
    /**
     * Create a new notification
     */
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }
    
    /**
     * Send notification to user
     */
    public Notification sendNotification(
            Long userId,
            String title,
            String message,
            NotificationType type,
            NotificationLevel level,
            Map<String, Object> data) {
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setLevel(level != null ? level : NotificationLevel.INFO);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        
        if (data != null) {
            try {
                ObjectNode node = objectMapper.valueToTree(data);
                notification.setExtraData(node);
            } catch (Exception e) {
                logger.error("Error serializing notification data: {}", e.getMessage());
            }
        }
        
        notification = notificationRepository.save(notification);
        
        // Send through all channels
        sendThroughAllChannels(notification, userId);
        
        return notification;
    }
    
    /**
     * Send transaction notification
     */
    public Notification sendTransactionNotification(
            Long userId,
            Long transactionId,
            String title,
            String message,
            NotificationType type) {
        
        Map<String, Object> data = new HashMap<>();
        data.put("transaction_id", transactionId);
        data.put("type", "transaction");
        
        return sendNotification(userId, title, message, type, NotificationLevel.INFO, data);
    }
    
    /**
     * Send through all notification channels
     */
    private void sendThroughAllChannels(Notification notification, Long userId) {
        try {
            // Send email notification
            if (emailNotificationService != null) {
                emailNotificationService.sendEmailNotification(
                    userId, notification.getTitle(), notification.getMessage(), notification.getExtraData());
            }
            
            // Send SMS notification
            if (smsNotificationService != null) {
                smsNotificationService.sendSMSNotification(
                    userId, notification.getTitle() + ": " + notification.getMessage());
            }
            
            // Send push notification
            if (pushNotificationService != null) {
                Map<String, String> pushData = new HashMap<>();
                if (notification.getExtraData() != null) {
                    notification.getExtraData().fieldNames().forEachRemaining(fieldName -> {
                        pushData.put(fieldName, notification.getExtraData().get(fieldName).asText());
                    });
                }
                pushNotificationService.sendPushNotification(
                    userId, notification.getTitle(), notification.getMessage(), pushData);
            }
            
            // Send WebSocket notification
            if (webSocketNotificationService != null) {
                webSocketNotificationService.sendWebSocketNotification(userId, notification);
            }
            
            // Update status to sent
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            logger.error("Error sending notification through channels: {}", e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Get all notifications with pagination
     */
    public Page<Notification> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }
    
    /**
     * Get user notifications
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get unread notifications for user
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, NotificationStatus.PENDING);
    }
    
    /**
     * Mark notification as read
     */
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
    
    /**
     * Update notification status
     */
    public Notification updateStatus(Long id, NotificationStatus status) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setStatus(status);
        if (status == NotificationStatus.READ) {
            notification.setReadAt(LocalDateTime.now());
        }
        return notificationRepository.save(notification);
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
    
    /**
     * Mark all notifications as read for user
     */
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
            .findByUserIdAndStatus(userId, NotificationStatus.PENDING);
        
        for (Notification notification : unreadNotifications) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Get notification statistics for user
     */
    public Map<String, Object> getNotificationStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        long total = notificationRepository.countByUserId(userId);
        long unread = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING);
        long read = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.READ);
        long failed = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.FAILED);
        
        stats.put("total", total);
        stats.put("unread", unread);
        stats.put("read", read);
        stats.put("failed", failed);
        stats.put("read_percentage", total > 0 ? (read * 100.0 / total) : 0);
        
        return stats;
    }
}