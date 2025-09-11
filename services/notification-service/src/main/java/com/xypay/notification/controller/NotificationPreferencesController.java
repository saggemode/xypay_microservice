package com.xypay.notification.controller;

import com.xypay.notification.domain.NotificationPreferences;
import com.xypay.notification.service.NotificationPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-preferences")
public class NotificationPreferencesController {
    
    @Autowired
    private NotificationPreferencesService preferencesService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<NotificationPreferences> getUserPreferences(@PathVariable Long userId) {
        return preferencesService.getUserPreferences(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<NotificationPreferences> createUserPreferences(@PathVariable Long userId) {
        NotificationPreferences preferences = preferencesService.createDefaultPreferences(userId);
        return ResponseEntity.ok(preferences);
    }
    
    @PutMapping("/user/{userId}")
    public ResponseEntity<NotificationPreferences> updateUserPreferences(
            @PathVariable Long userId, @RequestBody NotificationPreferences preferences) {
        NotificationPreferences updated = preferencesService.updatePreferences(userId, preferences);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/user/{userId}/allowed/{channel}/{notificationType}")
    public ResponseEntity<Boolean> isNotificationAllowed(
            @PathVariable Long userId,
            @PathVariable String channel,
            @PathVariable String notificationType) {
        boolean allowed = preferencesService.isNotificationAllowed(userId, channel, notificationType);
        return ResponseEntity.ok(allowed);
    }
    
    @GetMapping("/user/{userId}/quiet-hours")
    public ResponseEntity<Boolean> isInQuietHours(@PathVariable Long userId) {
        boolean inQuietHours = preferencesService.isInQuietHours(userId);
        return ResponseEntity.ok(inQuietHours);
    }
    
    @GetMapping("/email-enabled")
    public ResponseEntity<List<NotificationPreferences>> getUsersWithEmailEnabled() {
        List<NotificationPreferences> users = preferencesService.getUsersWithEmailEnabled();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/sms-enabled")
    public ResponseEntity<List<NotificationPreferences>> getUsersWithSmsEnabled() {
        List<NotificationPreferences> users = preferencesService.getUsersWithSmsEnabled();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/push-enabled")
    public ResponseEntity<List<NotificationPreferences>> getUsersWithPushEnabled() {
        List<NotificationPreferences> users = preferencesService.getUsersWithPushEnabled();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/in-app-enabled")
    public ResponseEntity<List<NotificationPreferences>> getUsersWithInAppEnabled() {
        List<NotificationPreferences> users = preferencesService.getUsersWithInAppEnabled();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/marketing-enabled/{channel}")
    public ResponseEntity<List<NotificationPreferences>> getUsersWithMarketingEnabled(@PathVariable String channel) {
        List<NotificationPreferences> users = preferencesService.getUsersWithMarketingEnabled(channel);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/digest-frequency/{frequency}")
    public ResponseEntity<List<NotificationPreferences>> getUsersByDigestFrequency(@PathVariable String frequency) {
        List<NotificationPreferences> users = preferencesService.getUsersByDigestFrequency(frequency);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/language/{language}")
    public ResponseEntity<List<NotificationPreferences>> getUsersByLanguage(@PathVariable String language) {
        List<NotificationPreferences> users = preferencesService.getUsersByLanguage(language);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/timezone/{timezone}")
    public ResponseEntity<List<NotificationPreferences>> getUsersByTimezone(@PathVariable String timezone) {
        List<NotificationPreferences> users = preferencesService.getUsersByTimezone(timezone);
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/bulk-update")
    public ResponseEntity<Void> bulkUpdatePreferences(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) request.get("userIds");
        @SuppressWarnings("unchecked")
        Map<String, Object> preferencesMap = (Map<String, Object>) request.get("preferences");
        
        NotificationPreferences templatePreferences = new NotificationPreferences();
        
        // Map the preferences from the request
        if (preferencesMap.containsKey("emailEnabled")) {
            templatePreferences.setEmailEnabled((Boolean) preferencesMap.get("emailEnabled"));
        }
        if (preferencesMap.containsKey("smsEnabled")) {
            templatePreferences.setSmsEnabled((Boolean) preferencesMap.get("smsEnabled"));
        }
        if (preferencesMap.containsKey("pushEnabled")) {
            templatePreferences.setPushEnabled((Boolean) preferencesMap.get("pushEnabled"));
        }
        if (preferencesMap.containsKey("inAppEnabled")) {
            templatePreferences.setInAppEnabled((Boolean) preferencesMap.get("inAppEnabled"));
        }
        if (preferencesMap.containsKey("digestFrequency")) {
            templatePreferences.setDigestFrequency((String) preferencesMap.get("digestFrequency"));
        }
        if (preferencesMap.containsKey("language")) {
            templatePreferences.setLanguage((String) preferencesMap.get("language"));
        }
        if (preferencesMap.containsKey("timezone")) {
            templatePreferences.setTimezone((String) preferencesMap.get("timezone"));
        }
        
        preferencesService.bulkUpdatePreferences(userIds, templatePreferences);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/user/{userId}/reset")
    public ResponseEntity<NotificationPreferences> resetToDefault(@PathVariable Long userId) {
        NotificationPreferences preferences = preferencesService.resetToDefault(userId);
        return ResponseEntity.ok(preferences);
    }
    
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUserPreferences(@PathVariable Long userId) {
        preferencesService.deleteUserPreferences(userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPreferencesStatistics() {
        Map<String, Object> stats = preferencesService.getPreferencesStatistics();
        return ResponseEntity.ok(stats);
    }
}
