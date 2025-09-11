package com.xypay.notification.controller;

import com.xypay.notification.domain.NotificationAnalytics;
import com.xypay.notification.service.NotificationAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notification-analytics")
public class NotificationAnalyticsController {
    
    @Autowired
    private NotificationAnalyticsService analyticsService;
    
    @PostMapping
    public ResponseEntity<NotificationAnalytics> createAnalyticsRecord(
            @RequestParam Long notificationId,
            @RequestParam Long userId,
            @RequestParam NotificationAnalytics.DeliveryChannel channel) {
        
        NotificationAnalytics analytics = analyticsService.createAnalyticsRecord(notificationId, userId, channel);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationAnalytics> getAnalyticsById(@PathVariable Long id) {
        return analyticsService.getAnalyticsById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/notification/{notificationId}")
    public ResponseEntity<List<NotificationAnalytics>> getAnalyticsForNotification(@PathVariable Long notificationId) {
        List<NotificationAnalytics> analytics = analyticsService.getAnalyticsForNotification(notificationId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationAnalytics>> getAnalyticsForUser(@PathVariable Long userId) {
        List<NotificationAnalytics> analytics = analyticsService.getAnalyticsForUser(userId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/user/{userId}/channel/{channel}")
    public ResponseEntity<List<NotificationAnalytics>> getUserAnalyticsByChannel(
            @PathVariable Long userId, @PathVariable NotificationAnalytics.DeliveryChannel channel) {
        List<NotificationAnalytics> analytics = analyticsService.getUserAnalyticsByChannel(userId, channel);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<NotificationAnalytics>> getUserAnalyticsByStatus(
            @PathVariable Long userId, @PathVariable NotificationAnalytics.DeliveryStatus status) {
        List<NotificationAnalytics> analytics = analyticsService.getUserAnalyticsByStatus(userId, status);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/channel/{channel}")
    public ResponseEntity<List<NotificationAnalytics>> getAnalyticsByChannel(
            @PathVariable NotificationAnalytics.DeliveryChannel channel) {
        List<NotificationAnalytics> analytics = analyticsService.getAnalyticsByChannel(channel);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationAnalytics>> getAnalyticsByStatus(
            @PathVariable NotificationAnalytics.DeliveryStatus status) {
        List<NotificationAnalytics> analytics = analyticsService.getAnalyticsByStatus(status);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/template/{templateKey}")
    public ResponseEntity<List<NotificationAnalytics>> getAnalyticsByTemplateKey(@PathVariable String templateKey) {
        List<NotificationAnalytics> analytics = analyticsService.getAnalyticsByTemplateKey(templateKey);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<NotificationAnalytics>> getAnalyticsByCampaignId(@PathVariable String campaignId) {
        List<NotificationAnalytics> analytics = analyticsService.getAnalyticsByCampaignId(campaignId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<NotificationAnalytics>> getAnalyticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<NotificationAnalytics> analytics = analyticsService.getAnalyticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/sent/date-range")
    public ResponseEntity<List<NotificationAnalytics>> getSentAnalyticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<NotificationAnalytics> analytics = analyticsService.getSentAnalyticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/delivered/date-range")
    public ResponseEntity<List<NotificationAnalytics>> getDeliveredAnalyticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<NotificationAnalytics> analytics = analyticsService.getDeliveredAnalyticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/read/date-range")
    public ResponseEntity<List<NotificationAnalytics>> getReadAnalyticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<NotificationAnalytics> analytics = analyticsService.getReadAnalyticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/failed/date-range")
    public ResponseEntity<List<NotificationAnalytics>> getFailedAnalyticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<NotificationAnalytics> analytics = analyticsService.getFailedAnalyticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDeliveryStatistics() {
        Map<String, Object> stats = analyticsService.getDeliveryStatistics();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/statistics/date-range")
    public ResponseEntity<Map<String, Object>> getDeliveryStatisticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> stats = analyticsService.getDeliveryStatisticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/statistics/template/{templateKey}")
    public ResponseEntity<Map<String, Object>> getTemplatePerformanceStatistics(@PathVariable String templateKey) {
        Map<String, Object> stats = analyticsService.getTemplatePerformanceStatistics(templateKey);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/statistics/campaign/{campaignId}")
    public ResponseEntity<Map<String, Object>> getCampaignPerformanceStatistics(@PathVariable String campaignId) {
        Map<String, Object> stats = analyticsService.getCampaignPerformanceStatistics(campaignId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/statistics/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserEngagementStatistics(@PathVariable Long userId) {
        Map<String, Object> stats = analyticsService.getUserEngagementStatistics(userId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/statistics/channel/{channel}")
    public ResponseEntity<Map<String, Object>> getChannelPerformanceStatistics(
            @PathVariable NotificationAnalytics.DeliveryChannel channel) {
        Map<String, Object> stats = analyticsService.getChannelPerformanceStatistics(channel);
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<NotificationAnalytics> updateAnalyticsRecord(
            @PathVariable Long id, @RequestBody NotificationAnalytics analytics) {
        analytics.setId(id);
        NotificationAnalytics updated = analyticsService.updateAnalyticsRecord(analytics);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalyticsRecord(@PathVariable Long id) {
        analyticsService.deleteAnalyticsRecord(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/notification/{notificationId}")
    public ResponseEntity<Void> deleteAnalyticsForNotification(@PathVariable Long notificationId) {
        analyticsService.deleteAnalyticsForNotification(notificationId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAnalyticsForUser(@PathVariable Long userId) {
        analyticsService.deleteAnalyticsForUser(userId);
        return ResponseEntity.ok().build();
    }
}
