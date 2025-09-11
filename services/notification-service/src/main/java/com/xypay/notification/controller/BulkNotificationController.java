package com.xypay.notification.controller;

import com.xypay.notification.domain.Notification;
import com.xypay.notification.domain.NotificationLevel;
import com.xypay.notification.domain.NotificationType;
import com.xypay.notification.service.BulkNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/bulk-notifications")
public class BulkNotificationController {
    
    @Autowired
    private BulkNotificationService bulkNotificationService;
    
    @PostMapping("/send")
    public ResponseEntity<CompletableFuture<List<Notification>>> sendBulkNotifications(
            @RequestParam List<Long> userIds,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam(required = false) NotificationLevel level,
            @RequestBody(required = false) Map<String, Object> data) {
        
        CompletableFuture<List<Notification>> future = bulkNotificationService.sendBulkNotifications(
            userIds, title, message, notificationType, level, data);
        
        return ResponseEntity.ok(future);
    }
    
    @PostMapping("/send-with-template")
    public ResponseEntity<CompletableFuture<List<Notification>>> sendBulkNotificationsWithTemplate(
            @RequestParam List<Long> userIds,
            @RequestParam String templateKey,
            @RequestParam NotificationType notificationType,
            @RequestParam(required = false) NotificationLevel level,
            @RequestBody Map<String, Object> templateVariables) {
        
        CompletableFuture<List<Notification>> future = bulkNotificationService.sendBulkNotificationsWithTemplate(
            userIds, templateKey, templateVariables, notificationType, level);
        
        return ResponseEntity.ok(future);
    }
    
    @PostMapping("/send-to-preferences")
    public ResponseEntity<CompletableFuture<List<Notification>>> sendBulkNotificationsToUsersWithPreferences(
            @RequestParam String channel,
            @RequestParam String notificationType,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType type,
            @RequestParam(required = false) NotificationLevel level,
            @RequestBody(required = false) Map<String, Object> data) {
        
        CompletableFuture<List<Notification>> future = bulkNotificationService.sendBulkNotificationsToUsersWithPreferences(
            channel, notificationType, title, message, type, level, data);
        
        return ResponseEntity.ok(future);
    }
    
    @PostMapping("/send-with-batching")
    public ResponseEntity<CompletableFuture<List<Notification>>> sendBulkNotificationsWithBatching(
            @RequestParam List<Long> userIds,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam(required = false) NotificationLevel level,
            @RequestParam(defaultValue = "100") int batchSize,
            @RequestBody(required = false) Map<String, Object> data) {
        
        CompletableFuture<List<Notification>> future = bulkNotificationService.sendBulkNotificationsWithBatching(
            userIds, title, message, notificationType, level, data, batchSize);
        
        return ResponseEntity.ok(future);
    }
    
    @PostMapping("/send-with-rate-limit")
    public ResponseEntity<CompletableFuture<List<Notification>>> sendBulkNotificationsWithRateLimit(
            @RequestParam List<Long> userIds,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam(required = false) NotificationLevel level,
            @RequestParam(defaultValue = "10") int notificationsPerSecond,
            @RequestBody(required = false) Map<String, Object> data) {
        
        CompletableFuture<List<Notification>> future = bulkNotificationService.sendBulkNotificationsWithRateLimit(
            userIds, title, message, notificationType, level, data, notificationsPerSecond);
        
        return ResponseEntity.ok(future);
    }
    
    @PostMapping("/send-with-priority")
    public ResponseEntity<CompletableFuture<List<Notification>>> sendBulkNotificationsWithPriority(
            @RequestParam List<Long> userIds,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam(required = false) NotificationLevel level,
            @RequestParam(defaultValue = "0") int priority,
            @RequestBody(required = false) Map<String, Object> data) {
        
        CompletableFuture<List<Notification>> future = bulkNotificationService.sendBulkNotificationsWithPriority(
            userIds, title, message, notificationType, level, data, priority);
        
        return ResponseEntity.ok(future);
    }
    
    @PostMapping("/send-with-campaign")
    public ResponseEntity<CompletableFuture<List<Notification>>> sendBulkNotificationsWithCampaign(
            @RequestParam List<Long> userIds,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam(required = false) NotificationLevel level,
            @RequestParam String campaignId,
            @RequestParam(required = false) String segmentId,
            @RequestBody(required = false) Map<String, Object> data) {
        
        CompletableFuture<List<Notification>> future = bulkNotificationService.sendBulkNotificationsWithCampaign(
            userIds, title, message, notificationType, level, data, campaignId, segmentId);
        
        return ResponseEntity.ok(future);
    }
}
