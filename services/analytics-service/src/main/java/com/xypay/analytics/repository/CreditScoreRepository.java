package com.xypay.analytics.repository;

import com.xypay.analytics.domain.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditScoreRepository extends JpaRepository<CreditScore, Long> {
    
    Optional<CreditScore> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    List<CreditScore> findByCustomerIdOrderByScoreDateDesc(Long customerId);
    
    List<CreditScore> findByRiskCategory(String riskCategory);
    
    List<CreditScore> findByIsActiveTrue();
    
    @Query("SELECT cs FROM CreditScore cs WHERE cs.score >= :minScore AND cs.score <= :maxScore")
    List<CreditScore> findByScoreRange(@Param("minScore") BigDecimal minScore, 
                                      @Param("maxScore") BigDecimal maxScore);
    
    @Query("SELECT cs FROM CreditScore cs WHERE cs.scoreDate >= :startDate AND cs.scoreDate <= :endDate")
    List<CreditScore> findByScoreDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(cs.score) FROM CreditScore cs WHERE cs.isActive = true")
    BigDecimal getAverageCreditScore();
    
    @Query("SELECT cs FROM CreditScore cs WHERE cs.defaultProbability >= :threshold")
    List<CreditScore> findByHighDefaultProbability(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT cs FROM CreditScore cs WHERE cs.customerId IN :customerIds AND cs.isActive = true")
    List<CreditScore> findByCustomerIdsAndIsActiveTrue(@Param("customerIds") List<Long> customerIds);
}
