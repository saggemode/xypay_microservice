package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SecurityHolding;
import com.xypay.xypay.domain.Portfolio;
import com.xypay.xypay.domain.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityHoldingRepository extends JpaRepository<SecurityHolding, UUID> {
    
    List<SecurityHolding> findByPortfolio(Portfolio portfolio);
    
    List<SecurityHolding> findBySecurity(Security security);
    
    Optional<SecurityHolding> findByPortfolioAndSecurity(Portfolio portfolio, Security security);
    
    List<SecurityHolding> findByPortfolioAndQuantityGreaterThan(Portfolio portfolio, BigDecimal quantity);
    
    List<SecurityHolding> findByIfrsStage(SecurityHolding.IfrsStage ifrsStage);
    
    List<SecurityHolding> findByIsRestrictedTrue();
    
    @Query("SELECT sh FROM SecurityHolding sh WHERE sh.marketValue BETWEEN :minValue AND :maxValue")
    List<SecurityHolding> findByMarketValueRange(@Param("minValue") BigDecimal minValue, 
                                               @Param("maxValue") BigDecimal maxValue);
    
    @Query("SELECT sh FROM SecurityHolding sh WHERE sh.unrealizedPnl > 0")
    List<SecurityHolding> findProfitableHoldings();
    
    @Query("SELECT sh FROM SecurityHolding sh WHERE sh.unrealizedPnl < 0")
    List<SecurityHolding> findLosingHoldings();
    
    @Query("SELECT COUNT(sh) FROM SecurityHolding sh WHERE sh.portfolio = :portfolio")
    Long countByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT SUM(sh.marketValue) FROM SecurityHolding sh WHERE sh.portfolio = :portfolio")
    BigDecimal getTotalMarketValueByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT SUM(sh.unrealizedPnl) FROM SecurityHolding sh WHERE sh.portfolio = :portfolio")
    BigDecimal getTotalUnrealizedPnlByPortfolio(@Param("portfolio") Portfolio portfolio);
}
