package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SecurityAlert;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SecurityAlert entity.
 */
@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {
    
    /**
     * Find alerts by affected user.
     */
    List<SecurityAlert> findByAffectedUser(User user);
    
    /**
     * Find alerts by status.
     */
    List<SecurityAlert> findByStatus(SecurityAlert.Status status);
    
    /**
     * Find alerts by alert type.
     */
    List<SecurityAlert> findByAlertType(SecurityAlert.AlertType alertType);
    
    /**
     * Find alerts by severity level.
     */
    List<SecurityAlert> findBySeverity(SecurityAlert.SeverityLevel severity);
    
    /**
     * Find open alerts for a user.
     */
    List<SecurityAlert> findByAffectedUserAndStatus(User user, SecurityAlert.Status status);
    
    /**
     * Count alerts by status.
     */
    long countByStatus(SecurityAlert.Status status);
    
    /**
     * Count alerts by timestamp range.
     */
    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Count alerts by type and timestamp range.
     */
    long countByAlertTypeAndTimestampBetween(SecurityAlert.AlertType alertType, LocalDateTime start, LocalDateTime end);
    
    /**
     * Count alerts by severity and timestamp range.
     */
    long countBySeverityAndTimestampBetween(SecurityAlert.SeverityLevel severity, LocalDateTime start, LocalDateTime end);
    
    /**
     * Find recent alerts for a user.
     */
    @Query("SELECT sa FROM SecurityAlert sa WHERE sa.affectedUser = :user AND sa.timestamp >= :since ORDER BY sa.timestamp DESC")
    List<SecurityAlert> findRecentAlertsByUser(@Param("user") User user, @Param("since") LocalDateTime since);
    
    /**
     * Find alerts by IP address.
     */
    List<SecurityAlert> findByIpAddress(String ipAddress);
    
    /**
     * Auto-resolve old alerts.
     */
    @Modifying
    @Query("UPDATE SecurityAlert sa SET sa.status = 'RESOLVED', sa.resolvedAt = :resolvedAt, sa.notes = CONCAT(COALESCE(sa.notes, ''), ' [Auto-resolved due to age]') WHERE sa.status = 'OPEN' AND sa.timestamp < :cutoffDate")
    int autoResolveOldAlerts(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("resolvedAt") LocalDateTime resolvedAt);
    
    /**
     * Auto-resolve old alerts (overloaded for convenience).
     */
    @Modifying
    @Query("UPDATE SecurityAlert sa SET sa.status = 'RESOLVED', sa.resolvedAt = CURRENT_TIMESTAMP, sa.notes = CONCAT(COALESCE(sa.notes, ''), ' [Auto-resolved due to age]') WHERE sa.status = 'OPEN' AND sa.timestamp < :cutoffDate")
    int autoResolveOldAlerts(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find unresolved high-severity alerts.
     */
    @Query("SELECT sa FROM SecurityAlert sa WHERE sa.status IN ('OPEN', 'INVESTIGATING') AND sa.severity IN ('HIGH', 'CRITICAL') ORDER BY sa.timestamp DESC")
    List<SecurityAlert> findUnresolvedHighSeverityAlerts();
    
    /**
     * Count alerts by user and time range.
     */
    long countByAffectedUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
}
