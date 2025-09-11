package com.xypay.analytics.repository;

import com.xypay.analytics.domain.RiskAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RiskAnalyticsRepository extends JpaRepository<RiskAnalytics, Long> {
    
    List<RiskAnalytics> findByCustomerId(Long customerId);
    
    List<RiskAnalytics> findByTransactionId(Long transactionId);
    
    List<RiskAnalytics> findByRiskType(String riskType);
    
    List<RiskAnalytics> findByRiskLevel(String riskLevel);
    
    List<RiskAnalytics> findByIsResolved(Boolean isResolved);
    
    @Query("SELECT ra FROM RiskAnalytics ra WHERE ra.riskScore >= :minScore AND ra.riskScore <= :maxScore")
    List<RiskAnalytics> findByRiskScoreRange(@Param("minScore") BigDecimal minScore, 
                                            @Param("maxScore") BigDecimal maxScore);
    
    @Query("SELECT ra FROM RiskAnalytics ra WHERE ra.assessmentDate >= :startDate AND ra.assessmentDate <= :endDate")
    List<RiskAnalytics> findByAssessmentDateRange(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ra FROM RiskAnalytics ra WHERE ra.riskScore >= :threshold")
    List<RiskAnalytics> findByHighRiskScore(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT AVG(ra.riskScore) FROM RiskAnalytics ra WHERE ra.riskType = :riskType")
    BigDecimal getAverageRiskScoreByType(@Param("riskType") String riskType);
    
    @Query("SELECT COUNT(ra) FROM RiskAnalytics ra WHERE ra.riskLevel = :riskLevel")
    Long countByRiskLevel(@Param("riskLevel") String riskLevel);
    
    @Query("SELECT ra FROM RiskAnalytics ra WHERE ra.customerId IN :customerIds")
    List<RiskAnalytics> findByCustomerIds(@Param("customerIds") List<Long> customerIds);
}
