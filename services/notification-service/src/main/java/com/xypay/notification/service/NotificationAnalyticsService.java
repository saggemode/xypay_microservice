package com.xypay.notification.service;

import com.xypay.notification.domain.NotificationAnalytics;
import com.xypay.notification.repository.NotificationAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationAnalyticsService {
    
    @Autowired
    private NotificationAnalyticsRepository analyticsRepository;
    
    /**
     * Create analytics record for notification
     */
    public NotificationAnalytics createAnalyticsRecord(Long notificationId, Long userId, 
                                                     NotificationAnalytics.DeliveryChannel channel) {
        NotificationAnalytics analytics = new NotificationAnalytics(notificationId, userId, channel);
        return analyticsRepository.save(analytics);
    }
    
    /**
     * Update analytics record
     */
    public NotificationAnalytics updateAnalyticsRecord(NotificationAnalytics analytics) {
        return analyticsRepository.save(analytics);
    }
    
    /**
     * Get analytics by ID
     */
    public Optional<NotificationAnalytics> getAnalyticsById(Long id) {
        return analyticsRepository.findById(id);
    }
    
    /**
     * Get analytics for notification
     */
    public List<NotificationAnalytics> getAnalyticsForNotification(Long notificationId) {
        return analyticsRepository.findByNotificationId(notificationId);
    }
    
    /**
     * Get analytics for user
     */
    public List<NotificationAnalytics> getAnalyticsForUser(Long userId) {
        return analyticsRepository.findByUserId(userId);
    }
    
    /**
     * Get analytics by channel
     */
    public List<NotificationAnalytics> getAnalyticsByChannel(NotificationAnalytics.DeliveryChannel channel) {
        return analyticsRepository.findByChannel(channel);
    }
    
    /**
     * Get analytics by status
     */
    public List<NotificationAnalytics> getAnalyticsByStatus(NotificationAnalytics.DeliveryStatus status) {
        return analyticsRepository.findByStatus(status);
    }
    
    /**
     * Get analytics by template key
     */
    public List<NotificationAnalytics> getAnalyticsByTemplateKey(String templateKey) {
        return analyticsRepository.findByTemplateKey(templateKey);
    }
    
    /**
     * Get analytics by campaign ID
     */
    public List<NotificationAnalytics> getAnalyticsByCampaignId(String campaignId) {
        return analyticsRepository.findByCampaignId(campaignId);
    }
    
    /**
     * Get analytics by date range
     */
    public List<NotificationAnalytics> getAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Get sent analytics by date range
     */
    public List<NotificationAnalytics> getSentAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findSentByDateRange(startDate, endDate);
    }
    
    /**
     * Get delivered analytics by date range
     */
    public List<NotificationAnalytics> getDeliveredAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findDeliveredByDateRange(startDate, endDate);
    }
    
    /**
     * Get read analytics by date range
     */
    public List<NotificationAnalytics> getReadAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findReadByDateRange(startDate, endDate);
    }
    
    /**
     * Get failed analytics by date range
     */
    public List<NotificationAnalytics> getFailedAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findFailedByDateRange(startDate, endDate);
    }
    
    /**
     * Get user analytics by channel
     */
    public List<NotificationAnalytics> getUserAnalyticsByChannel(Long userId, NotificationAnalytics.DeliveryChannel channel) {
        return analyticsRepository.findByUserIdAndChannel(userId, channel);
    }
    
    /**
     * Get user analytics by status
     */
    public List<NotificationAnalytics> getUserAnalyticsByStatus(Long userId, NotificationAnalytics.DeliveryStatus status) {
        return analyticsRepository.findByUserIdAndStatus(userId, status);
    }
    
    /**
     * Get analytics by channel and status
     */
    public List<NotificationAnalytics> getAnalyticsByChannelAndStatus(NotificationAnalytics.DeliveryChannel channel, 
                                                                     NotificationAnalytics.DeliveryStatus status) {
        return analyticsRepository.findByChannelAndStatus(channel, status);
    }
    
    /**
     * Get analytics by template key and status
     */
    public List<NotificationAnalytics> getAnalyticsByTemplateKeyAndStatus(String templateKey, 
                                                                         NotificationAnalytics.DeliveryStatus status) {
        return analyticsRepository.findByTemplateKeyAndStatus(templateKey, status);
    }
    
    /**
     * Get analytics by campaign ID and status
     */
    public List<NotificationAnalytics> getAnalyticsByCampaignIdAndStatus(String campaignId, 
                                                                        NotificationAnalytics.DeliveryStatus status) {
        return analyticsRepository.findByCampaignIdAndStatus(campaignId, status);
    }
    
    /**
     * Get delivery statistics
     */
    public Map<String, Object> getDeliveryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total counts by channel
        for (NotificationAnalytics.DeliveryChannel channel : NotificationAnalytics.DeliveryChannel.values()) {
            Long total = analyticsRepository.countByChannel(channel);
            stats.put("total_" + channel.getValue(), total);
        }
        
        // Total counts by status
        for (NotificationAnalytics.DeliveryStatus status : NotificationAnalytics.DeliveryStatus.values()) {
            Long total = analyticsRepository.countByStatus(status);
            stats.put("total_" + status.getValue(), total);
        }
        
        // Channel-specific status counts
        for (NotificationAnalytics.DeliveryChannel channel : NotificationAnalytics.DeliveryChannel.values()) {
            for (NotificationAnalytics.DeliveryStatus status : NotificationAnalytics.DeliveryStatus.values()) {
                Long count = analyticsRepository.countByChannelAndStatus(channel, status);
                stats.put(channel.getValue() + "_" + status.getValue(), count);
            }
        }
        
        return stats;
    }
    
    /**
     * Get delivery statistics by date range
     */
    public Map<String, Object> getDeliveryStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        Long total = analyticsRepository.countByDateRange(startDate, endDate);
        Long sent = analyticsRepository.countSentByDateRange(startDate, endDate);
        Long delivered = analyticsRepository.countDeliveredByDateRange(startDate, endDate);
        Long read = analyticsRepository.countReadByDateRange(startDate, endDate);
        Long failed = analyticsRepository.countFailedByDateRange(startDate, endDate);
        
        stats.put("total", total);
        stats.put("sent", sent);
        stats.put("delivered", delivered);
        stats.put("read", read);
        stats.put("failed", failed);
        
        // Calculate rates
        if (total > 0) {
            stats.put("sent_rate", (sent * 100.0) / total);
            stats.put("delivered_rate", (delivered * 100.0) / total);
            stats.put("read_rate", (read * 100.0) / total);
            stats.put("failed_rate", (failed * 100.0) / total);
        } else {
            stats.put("sent_rate", 0.0);
            stats.put("delivered_rate", 0.0);
            stats.put("read_rate", 0.0);
            stats.put("failed_rate", 0.0);
        }
        
        return stats;
    }
    
    /**
     * Get template performance statistics
     */
    public Map<String, Object> getTemplatePerformanceStatistics(String templateKey) {
        Map<String, Object> stats = new HashMap<>();
        
        Long total = analyticsRepository.countByTemplateKey(templateKey);
        stats.put("total", total);
        
        for (NotificationAnalytics.DeliveryStatus status : NotificationAnalytics.DeliveryStatus.values()) {
            Long count = analyticsRepository.countByTemplateKeyAndStatus(templateKey, status);
            stats.put(status.getValue(), count);
            
            if (total > 0) {
                stats.put(status.getValue() + "_rate", (count * 100.0) / total);
            } else {
                stats.put(status.getValue() + "_rate", 0.0);
            }
        }
        
        // Average processing time
        Double avgProcessingTime = analyticsRepository.getAverageProcessingTimeByTemplate(templateKey);
        stats.put("avg_processing_time_ms", avgProcessingTime != null ? avgProcessingTime : 0.0);
        
        return stats;
    }
    
    /**
     * Get campaign performance statistics
     */
    public Map<String, Object> getCampaignPerformanceStatistics(String campaignId) {
        Map<String, Object> stats = new HashMap<>();
        
        Long total = analyticsRepository.countByCampaignId(campaignId);
        stats.put("total", total);
        
        for (NotificationAnalytics.DeliveryStatus status : NotificationAnalytics.DeliveryStatus.values()) {
            Long count = analyticsRepository.countByCampaignIdAndStatus(campaignId, status);
            stats.put(status.getValue(), count);
            
            if (total > 0) {
                stats.put(status.getValue() + "_rate", (count * 100.0) / total);
            } else {
                stats.put(status.getValue() + "_rate", 0.0);
            }
        }
        
        // Average processing time
        Double avgProcessingTime = analyticsRepository.getAverageProcessingTimeByCampaign(campaignId);
        stats.put("avg_processing_time_ms", avgProcessingTime != null ? avgProcessingTime : 0.0);
        
        return stats;
    }
    
    /**
     * Get user engagement statistics
     */
    public Map<String, Object> getUserEngagementStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        Long total = analyticsRepository.countByUserId(userId);
        stats.put("total", total);
        
        for (NotificationAnalytics.DeliveryStatus status : NotificationAnalytics.DeliveryStatus.values()) {
            Long count = analyticsRepository.countByUserIdAndStatus(userId, status);
            stats.put(status.getValue(), count);
            
            if (total > 0) {
                stats.put(status.getValue() + "_rate", (count * 100.0) / total);
            } else {
                stats.put(status.getValue() + "_rate", 0.0);
            }
        }
        
        return stats;
    }
    
    /**
     * Get channel performance statistics
     */
    public Map<String, Object> getChannelPerformanceStatistics(NotificationAnalytics.DeliveryChannel channel) {
        Map<String, Object> stats = new HashMap<>();
        
        Long total = analyticsRepository.countByChannel(channel);
        stats.put("total", total);
        
        for (NotificationAnalytics.DeliveryStatus status : NotificationAnalytics.DeliveryStatus.values()) {
            Long count = analyticsRepository.countByChannelAndStatus(channel, status);
            stats.put(status.getValue(), count);
            
            if (total > 0) {
                stats.put(status.getValue() + "_rate", (count * 100.0) / total);
            } else {
                stats.put(status.getValue() + "_rate", 0.0);
            }
        }
        
        // Average processing time
        Double avgProcessingTime = analyticsRepository.getAverageProcessingTimeByChannel(channel);
        stats.put("avg_processing_time_ms", avgProcessingTime != null ? avgProcessingTime : 0.0);
        
        return stats;
    }
    
    /**
     * Delete analytics record
     */
    public void deleteAnalyticsRecord(Long id) {
        analyticsRepository.deleteById(id);
    }
    
    /**
     * Delete analytics for notification
     */
    public void deleteAnalyticsForNotification(Long notificationId) {
        List<NotificationAnalytics> analytics = analyticsRepository.findByNotificationId(notificationId);
        analyticsRepository.deleteAll(analytics);
    }
    
    /**
     * Delete analytics for user
     */
    public void deleteAnalyticsForUser(Long userId) {
        List<NotificationAnalytics> analytics = analyticsRepository.findByUserId(userId);
        analyticsRepository.deleteAll(analytics);
    }
}
