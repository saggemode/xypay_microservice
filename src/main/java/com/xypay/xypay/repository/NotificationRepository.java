package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.domain.NotificationStatus;
import com.xypay.xypay.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity with comprehensive query methods.
 * Equivalent to Django's notification model queries.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notifications by recipient with pagination.
     */
    Page<Notification> findByRecipient(User recipient, Pageable pageable);
    
    /**
     * Find unread notifications by recipient ordered by creation date.
     */
    List<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient);
    
    /**
     * Find notifications by recipient and type.
     */
    List<Notification> findByRecipientAndNotificationTypeOrderByCreatedAtDesc(User recipient, NotificationType type);
    
    /**
     * Find notifications by recipient and level.
     */
    List<Notification> findByRecipientAndLevelOrderByCreatedAtDesc(User recipient, NotificationLevel level);
    
    /**
     * Find notifications by recipient and status.
     */
    List<Notification> findByRecipientAndStatusOrderByCreatedAtDesc(User recipient, NotificationStatus status);
    
    /**
     * Find urgent notifications (high priority or critical level).
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false AND (n.priority >= 8 OR n.level = 'CRITICAL') ORDER BY n.createdAt DESC")
    List<Notification> findUrgentNotificationsByRecipient(@Param("recipient") User recipient);
    
    /**
     * Find actionable notifications (have action text and URL).
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.actionText IS NOT NULL AND n.actionUrl IS NOT NULL ORDER BY n.createdAt DESC")
    List<Notification> findActionableNotificationsByRecipient(@Param("recipient") User recipient);
    
    /**
     * Find notifications by date range.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientAndDateRange(@Param("recipient") User recipient, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count unread notifications by recipient.
     */
    long countByRecipientAndIsReadFalse(User recipient);
    
    /**
     * Count notifications by recipient and type.
     */
    long countByRecipientAndNotificationType(User recipient, NotificationType type);
    
    /**
     * Count notifications by recipient and level.
     */
    long countByRecipientAndLevel(User recipient, NotificationLevel level);
    
    /**
     * Count notifications by recipient and status.
     */
    long countByRecipientAndStatus(User recipient, NotificationStatus status);
    
    /**
     * Count notifications by type and date range.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.notificationType = :type AND n.createdAt BETWEEN :startDate AND :endDate")
    long countByTypeAndDateRange(@Param("type") NotificationType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count notifications by level and date range.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.level = :level AND n.createdAt BETWEEN :startDate AND :endDate")
    long countByLevelAndDateRange(@Param("level") NotificationLevel level, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count notifications by status and date range.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(@Param("status") NotificationStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find unread notifications by recipient.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipient(@Param("recipient") User recipient);
    
    /**
     * Count unread notifications by recipient.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    long countUnreadByRecipient(@Param("recipient") User recipient);
    
    /**
     * Bulk mark notifications as read.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = 'READ' WHERE n.id IN :notificationIds AND n.recipient = :recipient")
    int bulkMarkAsRead(@Param("notificationIds") List<Long> notificationIds, @Param("recipient") User recipient, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Bulk mark notifications as unread.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = false, n.readAt = null, n.status = 'delivered' WHERE n.id IN :notificationIds AND n.recipient = :recipient")
    int bulkMarkAsUnread(@Param("notificationIds") List<Long> notificationIds, @Param("recipient") User recipient);
    
    /**
     * Mark all notifications as read for a recipient.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = 'READ' WHERE n.recipient = :recipient AND n.isRead = false")
    int markAllAsReadByRecipient(@Param("recipient") User recipient, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Delete old notifications.
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find duplicate notifications to prevent spam.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.notificationType = :type AND n.orderId = :orderId AND n.isRead = false")
    List<Notification> findDuplicateNotifications(@Param("recipient") User recipient, @Param("type") NotificationType type, @Param("orderId") String orderId);
    
    /**
     * Get notification statistics by type.
     */
    @Query("SELECT n.notificationType, COUNT(n) FROM Notification n WHERE n.recipient = :recipient GROUP BY n.notificationType")
    List<Object[]> getNotificationStatsByType(@Param("recipient") User recipient);
    
    /**
     * Get notification statistics by level.
     */
    @Query("SELECT n.level, COUNT(n) FROM Notification n WHERE n.recipient = :recipient GROUP BY n.level")
    List<Object[]> getNotificationStatsByLevel(@Param("recipient") User recipient);
    
    /**
     * Get notification statistics by status.
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.recipient = :recipient GROUP BY n.status")
    List<Object[]> getNotificationStatsByStatus(@Param("recipient") User recipient);
}