package com.xypay.analytics.repository;

import com.xypay.analytics.domain.CustomerAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAnalyticsRepository extends JpaRepository<CustomerAnalytics, Long> {
    
    Optional<CustomerAnalytics> findByCustomerId(Long customerId);
    
    List<CustomerAnalytics> findByCustomerIdIn(List<Long> customerIds);
    
    @Query("SELECT ca FROM CustomerAnalytics ca WHERE ca.lastUpdated >= :since")
    List<CustomerAnalytics> findRecentlyUpdated(@Param("since") LocalDateTime since);
    
    @Query("SELECT ca FROM CustomerAnalytics ca WHERE ca.totalTransactionValue >= :minValue")
    List<CustomerAnalytics> findByMinTransactionValue(@Param("minValue") BigDecimal minValue);
    
    @Query("SELECT ca FROM CustomerAnalytics ca WHERE ca.averageAccountBalance >= :minBalance")
    List<CustomerAnalytics> findByMinAccountBalance(@Param("minBalance") BigDecimal minBalance);
    
    @Query("SELECT ca FROM CustomerAnalytics ca WHERE ca.riskFactorCount > :maxRiskFactors")
    List<CustomerAnalytics> findByHighRiskFactors(@Param("maxRiskFactors") BigDecimal maxRiskFactors);
    
    @Query("SELECT AVG(ca.totalTransactionValue) FROM CustomerAnalytics ca")
    BigDecimal getAverageTransactionValue();
    
    @Query("SELECT AVG(ca.averageAccountBalance) FROM CustomerAnalytics ca")
    BigDecimal getAverageAccountBalance();
    
    @Query("SELECT COUNT(ca) FROM CustomerAnalytics ca WHERE ca.customerId IN :customerIds")
    Long countByCustomerIds(@Param("customerIds") List<Long> customerIds);
}
