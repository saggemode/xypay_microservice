package com.xypay.xypay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mock WebSocket notification service.
 * This is a simplified implementation that logs notifications instead of sending them via WebSocket.
 * To enable real WebSocket functionality, add spring-boot-starter-websocket dependency.
 */
@Service
public class WebSocketNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);
    
    /**
     * Send real-time notification via WebSocket (mock implementation).
     *
     * @param userId The user ID
     * @param notificationData The notification data as JSON string
     * @return true if successful, false otherwise
     */
    public boolean sendWebSocketNotification(String userId, String notificationData) {
        logger.info("Mock WebSocket notification for user {}: {}", userId, notificationData);
        return true; // Mock implementation always returns success
    }
    
    /**
     * Register a WebSocket session for a user (mock implementation).
     *
     * @param userId The user ID
     * @param session The WebSocket session (not used in mock)
     */
    public void registerUserSession(String userId, Object session) {
        logger.info("Mock: Registered WebSocket session for user {}", userId);
    }
    
    /**
     * Unregister a WebSocket session for a user (mock implementation).
     *
     * @param userId The user ID
     */
    public void unregisterUserSession(String userId) {
        logger.info("Mock: Unregistered WebSocket session for user {}", userId);
    }
}