package com.xypay.xypay.repository;

import com.xypay.xypay.domain.AnomalyDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnomalyDetectionRepository extends JpaRepository<AnomalyDetection, UUID> {
    
    List<AnomalyDetection> findByUserIdOrderByDetectedAtDesc(UUID userId);
    
    List<AnomalyDetection> findByIsAnomalyTrueOrderByDetectedAtDesc();
    
    List<AnomalyDetection> findByInvestigationStatusOrderByDetectedAtDesc(AnomalyDetection.InvestigationStatus status);
    
    List<AnomalyDetection> findByRequiresInvestigationTrueOrderByDetectedAtDesc();
    
    List<AnomalyDetection> findByDetectedAtBetweenOrderByDetectedAtDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT ad FROM AnomalyDetection ad WHERE ad.user.id = :userId AND ad.isAnomaly = true ORDER BY ad.detectedAt DESC")
    List<AnomalyDetection> findAnomaliesByUser(@Param("userId") UUID userId);
    
    @Query("SELECT ad FROM AnomalyDetection ad WHERE ad.anomalyType = :anomalyType AND ad.isAnomaly = true ORDER BY ad.detectedAt DESC")
    List<AnomalyDetection> findAnomaliesByType(@Param("anomalyType") AnomalyDetection.AnomalyType anomalyType);
    
    @Query("SELECT ad FROM AnomalyDetection ad WHERE ad.confidence >= :minConfidence AND ad.isAnomaly = true ORDER BY ad.confidence DESC, ad.detectedAt DESC")
    List<AnomalyDetection> findHighConfidenceAnomalies(@Param("minConfidence") Double minConfidence);
}
