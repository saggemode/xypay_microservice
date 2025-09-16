package com.xypay.xypay.repository;

import com.xypay.xypay.domain.BehavioralAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BehavioralAnalysisRepository extends JpaRepository<BehavioralAnalysis, UUID> {
    
    Optional<BehavioralAnalysis> findByUser(UUID userId);
    
    List<BehavioralAnalysis> findByTrustScoreGreaterThanEqualOrderByTrustScoreDesc(Double minTrustScore);
    
    List<BehavioralAnalysis> findByRegularityScoreGreaterThanEqualOrderByRegularityScoreDesc(Double minRegularityScore);
    
    List<BehavioralAnalysis> findByRiskAppetiteGreaterThanEqualOrderByRiskAppetiteDesc(Double minRiskAppetite);
    
    @Query("SELECT ba FROM BehavioralAnalysis ba WHERE ba.user.id = :userId")
    Optional<BehavioralAnalysis> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT ba FROM BehavioralAnalysis ba WHERE ba.trustScore >= :minTrustScore AND ba.confidenceLevel >= :minConfidence ORDER BY ba.trustScore DESC")
    List<BehavioralAnalysis> findHighTrustUsers(@Param("minTrustScore") Double minTrustScore, @Param("minConfidence") Double minConfidence);
    
    @Query("SELECT ba FROM BehavioralAnalysis ba WHERE ba.riskAppetite >= :minRiskAppetite ORDER BY ba.riskAppetite DESC")
    List<BehavioralAnalysis> findHighRiskUsers(@Param("minRiskAppetite") Double minRiskAppetite);
}
