package com.xypay.notification.repository;

import com.xypay.notification.domain.ScheduledNotification;
import com.xypay.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledNotificationRepository extends JpaRepository<ScheduledNotification, Long> {
    
    List<ScheduledNotification> findByUserId(Long userId);
    
    List<ScheduledNotification> findByStatus(ScheduledNotification.ScheduleStatus status);
    
    List<ScheduledNotification> findByScheduleType(ScheduledNotification.ScheduleType scheduleType);
    
    List<ScheduledNotification> findByNotificationType(NotificationType notificationType);
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.status = 'SCHEDULED' AND s.scheduledFor <= :now ORDER BY s.scheduledFor ASC")
    List<ScheduledNotification> findReadyToSend(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.status = 'FAILED' AND s.retryCount < s.maxRetries AND (s.nextRetryAt IS NULL OR s.nextRetryAt <= :now) ORDER BY s.nextRetryAt ASC")
    List<ScheduledNotification> findReadyToRetry(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.status = 'SCHEDULED' AND s.scheduledFor < :cutoffTime")
    List<ScheduledNotification> findOverdueNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.userId = :userId AND s.status IN ('PENDING', 'SCHEDULED') ORDER BY s.scheduledFor ASC")
    List<ScheduledNotification> findPendingAndScheduledByUserId(@Param("userId") Long userId);
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.scheduleType = 'RECURRING' AND s.status = 'SENT'")
    List<ScheduledNotification> findRecurringNotifications();
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.scheduleType = 'CONDITIONAL' AND s.status = 'PENDING'")
    List<ScheduledNotification> findConditionalNotifications();
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate")
    List<ScheduledNotification> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.sentAt >= :startDate AND s.sentAt <= :endDate")
    List<ScheduledNotification> findSentByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM ScheduledNotification s WHERE s.status = 'PENDING'")
    Long countPendingNotifications();
    
    @Query("SELECT COUNT(s) FROM ScheduledNotification s WHERE s.status = 'SCHEDULED'")
    Long countScheduledNotifications();
    
    @Query("SELECT COUNT(s) FROM ScheduledNotification s WHERE s.status = 'SENT'")
    Long countSentNotifications();
    
    @Query("SELECT COUNT(s) FROM ScheduledNotification s WHERE s.status = 'FAILED'")
    Long countFailedNotifications();
    
    @Query("SELECT COUNT(s) FROM ScheduledNotification s WHERE s.userId = :userId AND s.status IN ('PENDING', 'SCHEDULED')")
    Long countPendingAndScheduledByUserId(@Param("userId") Long userId);
    
    @Query("SELECT s FROM ScheduledNotification s WHERE s.priority >= :minPriority ORDER BY s.priority DESC, s.scheduledFor ASC")
    List<ScheduledNotification> findByMinPriority(@Param("minPriority") Integer minPriority);
}
