package com.xypay.analytics.repository;

import com.xypay.analytics.domain.FraudRiskScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudRiskScoreRepository extends JpaRepository<FraudRiskScore, Long> {
    
    Optional<FraudRiskScore> findByTransactionId(Long transactionId);
    
    List<FraudRiskScore> findByTransactionIdIn(List<Long> transactionIds);
    
    List<FraudRiskScore> findByRiskLevel(String riskLevel);
    
    List<FraudRiskScore> findByIsProcessed(Boolean isProcessed);
    
    @Query("SELECT frs FROM FraudRiskScore frs WHERE frs.riskScore >= :minScore AND frs.riskScore <= :maxScore")
    List<FraudRiskScore> findByRiskScoreRange(@Param("minScore") BigDecimal minScore, 
                                             @Param("maxScore") BigDecimal maxScore);
    
    @Query("SELECT frs FROM FraudRiskScore frs WHERE frs.assessmentDate >= :startDate AND frs.assessmentDate <= :endDate")
    List<FraudRiskScore> findByAssessmentDateRange(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT frs FROM FraudRiskScore frs WHERE frs.riskScore >= :threshold")
    List<FraudRiskScore> findByHighRiskScore(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT AVG(frs.riskScore) FROM FraudRiskScore frs")
    BigDecimal getAverageRiskScore();
    
    @Query("SELECT COUNT(frs) FROM FraudRiskScore frs WHERE frs.riskLevel = :riskLevel")
    Long countByRiskLevel(@Param("riskLevel") String riskLevel);
    
    @Query("SELECT frs FROM FraudRiskScore frs WHERE frs.transactionId IN :transactionIds")
    List<FraudRiskScore> findByTransactionIds(@Param("transactionIds") List<Long> transactionIds);
}
