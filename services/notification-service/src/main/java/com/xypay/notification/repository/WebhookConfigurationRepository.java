package com.xypay.notification.repository;

import com.xypay.notification.domain.WebhookConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookConfigurationRepository extends JpaRepository<WebhookConfiguration, Long> {
    
    List<WebhookConfiguration> findByIsActiveTrue();
    
    List<WebhookConfiguration> findByStatus(WebhookConfiguration.WebhookStatus status);
    
    List<WebhookConfiguration> findByEventsContainingAndIsActiveTrue(String event);
    
    @Query("SELECT w FROM WebhookConfiguration w WHERE w.isActive = true AND w.events LIKE %:event%")
    List<WebhookConfiguration> findActiveWebhooksByEvent(@Param("event") String event);
    
    @Query("SELECT w FROM WebhookConfiguration w WHERE w.isActive = true AND w.status = 'ACTIVE'")
    List<WebhookConfiguration> findActiveAndHealthyWebhooks();
    
    @Query("SELECT w FROM WebhookConfiguration w WHERE w.failureCount > w.successCount")
    List<WebhookConfiguration> findUnhealthyWebhooks();
    
    @Query("SELECT w FROM WebhookConfiguration w WHERE w.lastTriggeredAt IS NULL OR w.lastTriggeredAt < :cutoffTime")
    List<WebhookConfiguration> findInactiveWebhooks(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(w) FROM WebhookConfiguration w WHERE w.isActive = true")
    Long countActiveWebhooks();
    
    @Query("SELECT COUNT(w) FROM WebhookConfiguration w WHERE w.isActive = true AND w.status = 'ACTIVE'")
    Long countActiveAndHealthyWebhooks();
    
    @Query("SELECT COUNT(w) FROM WebhookConfiguration w WHERE w.failureCount > w.successCount")
    Long countUnhealthyWebhooks();
}
