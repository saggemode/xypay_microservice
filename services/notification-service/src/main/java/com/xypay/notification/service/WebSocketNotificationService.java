package com.xypay.notification.service;

import com.xypay.notification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send WebSocket notification to user
     */
    public boolean sendWebSocketNotification(Long userId, Notification notification) {
        try {
            String destination = "/user/" + userId + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.info("WebSocket notification sent to user {}: {}", userId, notification.getTitle());
            return true;
            
        } catch (Exception e) {
            logger.error("WebSocket notification failed for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Send real-time notification to all connected users
     */
    public boolean sendBroadcastNotification(Notification notification) {
        try {
            String destination = "/topic/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.info("Broadcast notification sent: {}", notification.getTitle());
            return true;
            
        } catch (Exception e) {
            logger.error("Broadcast notification failed: {}", e.getMessage());
            return false;
        }
    }
}
