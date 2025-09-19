package com.xypay.xypay.repository;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for UserSession entity.
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    
    /**
     * Find session by session key.
     */
    UserSession findBySessionKey(String sessionKey);
    
    /**
     * Find sessions by user.
     */
    List<UserSession> findByUser(User user);
    
    /**
     * Find active sessions by user.
     */
    List<UserSession> findByUserAndIsActive(User user, Boolean isActive);
    
    /**
     * Find sessions by IP address.
     */
    List<UserSession> findByIpAddress(String ipAddress);
    
    /**
     * Count sessions by active status.
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * Count sessions by creation date range.
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find distinct IP addresses for a user.
     */
    @Query("SELECT DISTINCT us.ipAddress FROM UserSession us WHERE us.user = :user")
    List<String> findDistinctIpAddressesByUser(@Param("user") User user);
    
    /**
     * Find recent sessions for a user.
     */
    @Query("SELECT us FROM UserSession us WHERE us.user = :user AND us.createdAt >= :since ORDER BY us.lastActivity DESC")
    List<UserSession> findRecentSessionsByUser(@Param("user") User user, @Param("since") LocalDateTime since);
    
    /**
     * Deactivate sessions by user.
     */
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false WHERE us.user = :user")
    int deactivateSessionsByUser(@Param("user") User user);
    
    /**
     * Deactivate old sessions.
     */
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false WHERE us.lastActivity < :cutoffTime AND us.isActive = true")
    int deactivateOldSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Delete old sessions.
     */
    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find sessions with suspicious activity.
     */
    @Query("SELECT us FROM UserSession us WHERE us.user = :user AND us.ipAddress != :currentIp AND us.isActive = true")
    List<UserSession> findSuspiciousSessionsForUser(@Param("user") User user, @Param("currentIp") String currentIp);
    
    /**
     * Count active sessions by user.
     */
    long countByUserAndIsActive(User user, Boolean isActive);
}
