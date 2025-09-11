package com.xypay.notification.controller;

import com.xypay.notification.domain.Notification;
import com.xypay.notification.domain.NotificationLevel;
import com.xypay.notification.domain.NotificationStatus;
import com.xypay.notification.domain.NotificationType;
import com.xypay.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<Notification>> getAllNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationService.getAllNotifications(pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        Notification created = notificationService.createNotification(notification);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType type,
            @RequestParam(required = false) NotificationLevel level,
            @RequestBody(required = false) Map<String, Object> data) {
        
        Notification notification = notificationService.sendNotification(
            userId, title, message, type, level, data);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/send-transaction")
    public ResponseEntity<Notification> sendTransactionNotification(
            @RequestParam Long userId,
            @RequestParam Long transactionId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType type) {
        
        Notification notification = notificationService.sendTransactionNotification(
            userId, transactionId, title, message, type);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Notification> updateStatus(
            @PathVariable Long id,
            @RequestParam NotificationStatus status) {
        Notification notification = notificationService.updateStatus(id, status);
        return ResponseEntity.ok(notification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk/read")
    public ResponseEntity<Void> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationStats(@PathVariable Long userId) {
        Map<String, Object> stats = notificationService.getNotificationStats(userId);
        return ResponseEntity.ok(stats);
    }
}
