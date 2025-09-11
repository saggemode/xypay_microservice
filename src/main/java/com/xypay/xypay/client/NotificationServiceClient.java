package com.xypay.xypay.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {
    
    @GetMapping("/api/notifications/user/{userId}")
    List<Map<String, Object>> getUserNotifications(@PathVariable("userId") Long userId);
    
    @GetMapping("/api/notifications/user/{userId}/unread")
    List<Map<String, Object>> getUnreadNotifications(@PathVariable("userId") Long userId);
    
    @PostMapping("/api/notifications/send")
    Map<String, Object> sendNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam("type") String type,
            @RequestBody(required = false) Map<String, Object> data);
    
    @PutMapping("/api/notifications/{id}/read")
    Map<String, Object> markAsRead(@PathVariable("id") Long id);
    
    @GetMapping("/api/notifications/stats/{userId}")
    Map<String, Object> getNotificationStats(@PathVariable("userId") Long userId);
}
