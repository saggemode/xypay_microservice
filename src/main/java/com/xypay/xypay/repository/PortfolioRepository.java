package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    List<Portfolio> findByBankId(Long bankId);
    
    List<Portfolio> findByCustomerId(Long customerId);
    
    List<Portfolio> findByPortfolioType(Portfolio.PortfolioType portfolioType);
    
    List<Portfolio> findByStatus(Portfolio.PortfolioStatus status);
    
    List<Portfolio> findByRiskProfile(Portfolio.RiskProfile riskProfile);
    
    List<Portfolio> findByInvestmentObjective(Portfolio.InvestmentObjective objective);
    
    List<Portfolio> findByBaseCurrency(String baseCurrency);
    
    List<Portfolio> findByPortfolioManagerId(Long portfolioManagerId);
    
    List<Portfolio> findByShariaCompliant(Boolean shariaCompliant);
    
    @Query("SELECT p FROM Portfolio p WHERE p.totalMarketValue BETWEEN :minValue AND :maxValue")
    List<Portfolio> findByMarketValueRange(@Param("minValue") BigDecimal minValue, 
                                         @Param("maxValue") BigDecimal maxValue);
    
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.status = 'ACTIVE'")
    Long countActivePortfolios();
    
    @Query("SELECT SUM(p.totalMarketValue) FROM Portfolio p WHERE p.status = 'ACTIVE'")
    BigDecimal getTotalAssetsUnderManagement();
    
    @Query("SELECT p FROM Portfolio p WHERE p.portfolioNumber = :portfolioNumber")
    Portfolio findByPortfolioNumber(@Param("portfolioNumber") String portfolioNumber);
}
