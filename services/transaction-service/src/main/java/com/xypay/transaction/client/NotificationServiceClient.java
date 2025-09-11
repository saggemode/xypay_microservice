package com.xypay.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationRequest request);
    
    class NotificationRequest {
        public Long userId;
        public String title;
        public String message;
        public String type;
        public String channel;
        public String recipient;
        
        public NotificationRequest() {}
        
        public NotificationRequest(Long userId, String title, String message, String type, String channel, String recipient) {
            this.userId = userId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.channel = channel;
            this.recipient = recipient;
        }
    }
}
