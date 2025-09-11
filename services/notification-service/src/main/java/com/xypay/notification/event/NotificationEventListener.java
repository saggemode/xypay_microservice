package com.xypay.notification.event;

import com.xypay.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    
    @KafkaListener(topics = "notification-events", groupId = "notification-service-group")
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            log.info("Received notification event: {} for user: {}", 
                    event.getEventType(), event.getUserId());
            
            switch (event.getEventType()) {
                case "TRANSACTION_NOTIFICATION":
                    handleTransactionNotification(event);
                    break;
                case "ACCOUNT_NOTIFICATION":
                    handleAccountNotification(event);
                    break;
                case "SECURITY_NOTIFICATION":
                    handleSecurityNotification(event);
                    break;
                case "MARKETING_NOTIFICATION":
                    handleMarketingNotification(event);
                    break;
                default:
                    log.warn("Unknown notification event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }
    
    private void handleTransactionNotification(NotificationEvent event) {
        log.info("Processing transaction notification for user: {}", event.getUserId());
        // Send transaction-related notifications
        // Email, SMS, Push notifications
    }
    
    private void handleAccountNotification(NotificationEvent event) {
        log.info("Processing account notification for user: {}", event.getUserId());
        // Send account-related notifications
        // Balance updates, account changes, etc.
    }
    
    private void handleSecurityNotification(NotificationEvent event) {
        log.info("Processing security notification for user: {}", event.getUserId());
        // Send security-related notifications
        // Login alerts, suspicious activity, etc.
    }
    
    private void handleMarketingNotification(NotificationEvent event) {
        log.info("Processing marketing notification for user: {}", event.getUserId());
        // Send marketing notifications
        // Promotions, offers, etc.
    }
    
    // NotificationEvent class for deserialization
    public static class NotificationEvent {
        public Long userId;
        public String eventType;
        public String title;
        public String message;
        public String channel;
        public String recipient;
        public java.time.LocalDateTime eventTimestamp;
        
        public Long getUserId() {
            return userId;
        }
        
        public String getEventType() {
            return eventType;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getChannel() {
            return channel;
        }
        
        public String getRecipient() {
            return recipient;
        }
        
        public java.time.LocalDateTime getEventTimestamp() {
            return eventTimestamp;
        }
    }
}