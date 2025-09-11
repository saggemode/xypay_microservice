package com.xypay.notification.repository;

import com.xypay.notification.domain.NotificationAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationAnalyticsRepository extends JpaRepository<NotificationAnalytics, Long> {
    
    List<NotificationAnalytics> findByNotificationId(Long notificationId);
    
    List<NotificationAnalytics> findByUserId(Long userId);
    
    List<NotificationAnalytics> findByChannel(NotificationAnalytics.DeliveryChannel channel);
    
    List<NotificationAnalytics> findByStatus(NotificationAnalytics.DeliveryStatus status);
    
    List<NotificationAnalytics> findByTemplateKey(String templateKey);
    
    List<NotificationAnalytics> findByCampaignId(String campaignId);
    
    List<NotificationAnalytics> findBySegmentId(String segmentId);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.createdAt >= :startDate AND a.createdAt <= :endDate")
    List<NotificationAnalytics> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.sentAt >= :startDate AND a.sentAt <= :endDate")
    List<NotificationAnalytics> findSentByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.deliveredAt >= :startDate AND a.deliveredAt <= :endDate")
    List<NotificationAnalytics> findDeliveredByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.readAt >= :startDate AND a.readAt <= :endDate")
    List<NotificationAnalytics> findReadByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.failedAt >= :startDate AND a.failedAt <= :endDate")
    List<NotificationAnalytics> findFailedByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.userId = :userId AND a.channel = :channel ORDER BY a.createdAt DESC")
    List<NotificationAnalytics> findByUserIdAndChannel(@Param("userId") Long userId, @Param("channel") NotificationAnalytics.DeliveryChannel channel);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.userId = :userId AND a.status = :status ORDER BY a.createdAt DESC")
    List<NotificationAnalytics> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.channel = :channel AND a.status = :status")
    List<NotificationAnalytics> findByChannelAndStatus(@Param("channel") NotificationAnalytics.DeliveryChannel channel, @Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.templateKey = :templateKey AND a.status = :status")
    List<NotificationAnalytics> findByTemplateKeyAndStatus(@Param("templateKey") String templateKey, @Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT a FROM NotificationAnalytics a WHERE a.campaignId = :campaignId AND a.status = :status")
    List<NotificationAnalytics> findByCampaignIdAndStatus(@Param("campaignId") String campaignId, @Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.channel = :channel")
    Long countByChannel(@Param("channel") NotificationAnalytics.DeliveryChannel channel);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.status = :status")
    Long countByStatus(@Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.channel = :channel AND a.status = :status")
    Long countByChannelAndStatus(@Param("channel") NotificationAnalytics.DeliveryChannel channel, @Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.templateKey = :templateKey")
    Long countByTemplateKey(@Param("templateKey") String templateKey);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.templateKey = :templateKey AND a.status = :status")
    Long countByTemplateKeyAndStatus(@Param("templateKey") String templateKey, @Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.campaignId = :campaignId")
    Long countByCampaignId(@Param("campaignId") String campaignId);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.campaignId = :campaignId AND a.status = :status")
    Long countByCampaignIdAndStatus(@Param("campaignId") String campaignId, @Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.userId = :userId AND a.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") NotificationAnalytics.DeliveryStatus status);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.createdAt >= :startDate AND a.createdAt <= :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.sentAt >= :startDate AND a.sentAt <= :endDate")
    Long countSentByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.deliveredAt >= :startDate AND a.deliveredAt <= :endDate")
    Long countDeliveredByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.readAt >= :startDate AND a.readAt <= :endDate")
    Long countReadByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM NotificationAnalytics a WHERE a.failedAt >= :startDate AND a.failedAt <= :endDate")
    Long countFailedByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(a.processingTimeMs) FROM NotificationAnalytics a WHERE a.processingTimeMs IS NOT NULL AND a.channel = :channel")
    Double getAverageProcessingTimeByChannel(@Param("channel") NotificationAnalytics.DeliveryChannel channel);
    
    @Query("SELECT AVG(a.processingTimeMs) FROM NotificationAnalytics a WHERE a.processingTimeMs IS NOT NULL AND a.templateKey = :templateKey")
    Double getAverageProcessingTimeByTemplate(@Param("templateKey") String templateKey);
    
    @Query("SELECT AVG(a.processingTimeMs) FROM NotificationAnalytics a WHERE a.processingTimeMs IS NOT NULL AND a.campaignId = :campaignId")
    Double getAverageProcessingTimeByCampaign(@Param("campaignId") String campaignId);
}
