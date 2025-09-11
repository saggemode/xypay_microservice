package com.xypay.treasury.repository;

import com.xypay.treasury.domain.RiskMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RiskMetricsRepository extends JpaRepository<RiskMetrics, Long> {
    
    List<RiskMetrics> findByRiskToleranceBreachTrue();
    
    List<RiskMetrics> findByRiskRating(String riskRating);
    
    @Query("SELECT rm FROM RiskMetrics rm WHERE rm.createdAt >= :startDate ORDER BY rm.createdAt DESC")
    List<RiskMetrics> findRecentRiskMetrics(@Param("startDate") LocalDate startDate);
    
    @Query("SELECT rm FROM RiskMetrics rm WHERE rm.totalRisk > :threshold ORDER BY rm.totalRisk DESC")
    List<RiskMetrics> findHighRiskMetrics(@Param("threshold") java.math.BigDecimal threshold);
}
