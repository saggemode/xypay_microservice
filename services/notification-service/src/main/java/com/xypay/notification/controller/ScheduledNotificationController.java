package com.xypay.notification.controller;

import com.xypay.notification.domain.NotificationType;
import com.xypay.notification.domain.ScheduledNotification;
import com.xypay.notification.service.ScheduledNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/scheduled-notifications")
public class ScheduledNotificationController {
    
    @Autowired
    private ScheduledNotificationService scheduledNotificationService;
    
    @PostMapping("/schedule")
    public ResponseEntity<ScheduledNotification> scheduleNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledFor) {
        
        ScheduledNotification scheduled = scheduledNotificationService.scheduleNotification(
            userId, title, message, notificationType, scheduledFor);
        return ResponseEntity.ok(scheduled);
    }
    
    @PostMapping("/schedule-delayed")
    public ResponseEntity<ScheduledNotification> scheduleDelayedNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam int delayMinutes) {
        
        ScheduledNotification scheduled = scheduledNotificationService.scheduleDelayedNotification(
            userId, title, message, notificationType, delayMinutes);
        return ResponseEntity.ok(scheduled);
    }
    
    @PostMapping("/schedule-recurring")
    public ResponseEntity<ScheduledNotification> scheduleRecurringNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam String cronExpression) {
        
        ScheduledNotification scheduled = scheduledNotificationService.scheduleRecurringNotification(
            userId, title, message, notificationType, cronExpression);
        return ResponseEntity.ok(scheduled);
    }
    
    @PostMapping("/schedule-conditional")
    public ResponseEntity<ScheduledNotification> scheduleConditionalNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType notificationType,
            @RequestParam String conditionExpression) {
        
        ScheduledNotification scheduled = scheduledNotificationService.scheduleConditionalNotification(
            userId, title, message, notificationType, conditionExpression);
        return ResponseEntity.ok(scheduled);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ScheduledNotification> getScheduledNotification(@PathVariable Long id) {
        return scheduledNotificationService.getScheduledNotification(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScheduledNotification>> getUserScheduledNotifications(@PathVariable Long userId) {
        List<ScheduledNotification> notifications = scheduledNotificationService.getUserScheduledNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelScheduledNotification(@PathVariable Long id) {
        scheduledNotificationService.cancelScheduledNotification(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ScheduledNotification> updateScheduledNotification(
            @PathVariable Long id, @RequestBody ScheduledNotification notification) {
        notification.setId(id);
        ScheduledNotification updated = scheduledNotificationService.updateScheduledNotification(notification);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheduledNotification(@PathVariable Long id) {
        scheduledNotificationService.deleteScheduledNotification(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/user/{userId}/bulk-cancel")
    public ResponseEntity<Void> bulkCancelUserNotifications(@PathVariable Long userId) {
        scheduledNotificationService.bulkCancelUserNotifications(userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getUserScheduledNotificationCount(@PathVariable Long userId) {
        Long count = scheduledNotificationService.getUserScheduledNotificationCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<ScheduledNotification>> getNotificationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<ScheduledNotification> notifications = scheduledNotificationService.getNotificationsByDateRange(startDate, endDate);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/sent/date-range")
    public ResponseEntity<List<ScheduledNotification>> getSentNotificationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<ScheduledNotification> notifications = scheduledNotificationService.getSentNotificationsByDateRange(startDate, endDate);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/high-priority")
    public ResponseEntity<List<ScheduledNotification>> getHighPriorityNotifications(
            @RequestParam(defaultValue = "8") Integer minPriority) {
        
        List<ScheduledNotification> notifications = scheduledNotificationService.getHighPriorityNotifications(minPriority);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getScheduledNotificationStatistics() {
        Map<String, Object> stats = scheduledNotificationService.getScheduledNotificationStatistics();
        return ResponseEntity.ok(stats);
    }
}
