package com.xypay.xypay.repository;

import com.xypay.xypay.domain.RealTimeMonitoring;
import com.xypay.xypay.enums.SecurityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RealTimeMonitoringRepository extends JpaRepository<RealTimeMonitoring, UUID> {
    
    List<RealTimeMonitoring> findByUserOrderByMonitoredAtDesc(UUID userId);
    
    List<RealTimeMonitoring> findByAlertLevelOrderByMonitoredAtDesc(SecurityLevel alertLevel);
    
    List<RealTimeMonitoring> findByRequiresReviewTrueOrderByMonitoredAtDesc();
    
    List<RealTimeMonitoring> findByMonitoredAtBetweenOrderByMonitoredAtDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT rtm FROM RealTimeMonitoring rtm WHERE rtm.user.id = :userId AND rtm.monitoredAt >= :startDate ORDER BY rtm.monitoredAt DESC")
    List<RealTimeMonitoring> findByUserAndMonitoredAtAfter(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT rtm FROM RealTimeMonitoring rtm WHERE rtm.alertLevel = :alertLevel AND rtm.requiresReview = true ORDER BY rtm.monitoredAt DESC")
    List<RealTimeMonitoring> findHighRiskAlerts(@Param("alertLevel") SecurityLevel alertLevel);
    
    @Query("SELECT rtm FROM RealTimeMonitoring rtm WHERE rtm.user.id = :userId AND rtm.alertLevel IN :levels ORDER BY rtm.monitoredAt DESC")
    List<RealTimeMonitoring> findByUserAndAlertLevelIn(@Param("userId") UUID userId, @Param("levels") List<SecurityLevel> levels);
}
