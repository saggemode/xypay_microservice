package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SecurityAlert;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.enums.SecurityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for SecurityAlert entity.
 */
@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, UUID> {
    
    /**
     * Find alerts by user.
     */
    List<SecurityAlert> findByUser(User user);
    
    /**
     * Find alerts by read status.
     */
    List<SecurityAlert> findByIsRead(Boolean isRead);
    
    /**
     * Find alerts by alert type.
     */
    List<SecurityAlert> findByAlertType(SecurityAlert.AlertType alertType);
    
    /**
     * Find alerts by severity level.
     */
    List<SecurityAlert> findBySeverity(SecurityLevel severity);
    
    /**
     * Find alerts for a user by read status.
     */
    List<SecurityAlert> findByUserAndIsRead(User user, Boolean isRead);
    
    /**
     * Count alerts by read status.
     */
    long countByIsRead(Boolean isRead);
    
    /**
     * Count alerts by timestamp range.
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Count alerts by type and timestamp range.
     */
    long countByAlertTypeAndCreatedAtBetween(SecurityAlert.AlertType alertType, LocalDateTime start, LocalDateTime end);
    
    /**
     * Count alerts by severity and timestamp range.
     */
    long countBySeverityAndCreatedAtBetween(SecurityLevel severity, LocalDateTime start, LocalDateTime end);
    
    /**
     * Find recent alerts for a user.
     */
    @Query("SELECT sa FROM SecurityAlert sa WHERE sa.user = :user AND sa.createdAt >= :since ORDER BY sa.createdAt DESC")
    List<SecurityAlert> findRecentAlertsByUser(@Param("user") User user, @Param("since") LocalDateTime since);
    
    /**
     * Find alerts by IP address.
     */
    List<SecurityAlert> findByIpAddress(String ipAddress);
    
    /**
     * Auto-resolve old alerts.
     */
    @Modifying
    @Query("UPDATE SecurityAlert sa SET sa.isResolved = true, sa.readAt = :resolvedAt WHERE sa.isResolved = false AND sa.createdAt < :cutoffDate")
    int autoResolveOldAlerts(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("resolvedAt") LocalDateTime resolvedAt);
    
    /**
     * Auto-resolve old alerts (overloaded for convenience).
     */
    @Modifying
    @Query("UPDATE SecurityAlert sa SET sa.isResolved = true, sa.readAt = CURRENT_TIMESTAMP WHERE sa.isResolved = false AND sa.createdAt < :cutoffDate")
    int autoResolveOldAlerts(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find unresolved high-severity alerts.
     */
    @Query("SELECT sa FROM SecurityAlert sa WHERE sa.isResolved = false AND sa.severity IN ('HIGH', 'CRITICAL') ORDER BY sa.createdAt DESC")
    List<SecurityAlert> findUnresolvedHighSeverityAlerts();
    
    /**
     * Count alerts by user and time range.
     */
    long countByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}
