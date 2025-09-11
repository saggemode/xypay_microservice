package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityRepository extends JpaRepository<Security, Long> {
    
    Optional<Security> findBySymbol(String symbol);
    
    Optional<Security> findByIsin(String isin);
    
    Optional<Security> findByCusip(String cusip);
    
    List<Security> findBySecurityType(Security.SecurityType securityType);
    
    List<Security> findByAssetClass(Security.AssetClass assetClass);
    
    List<Security> findByExchange(String exchange);
    
    List<Security> findByCountryCode(String countryCode);
    
    List<Security> findByCurrencyCode(String currencyCode);
    
    List<Security> findBySector(String sector);
    
    List<Security> findByIndustry(String industry);
    
    List<Security> findByIssuer(String issuer);
    
    List<Security> findByIsActiveTrue();
    
    List<Security> findByIsTradeableTrue();
    
    List<Security> findByIsLiquidTrue();
    
    List<Security> findByShariaCompliant(Boolean shariaCompliant);
    
    List<Security> findBySukukStructure(Security.SukukStructure sukukStructure);
    
    List<Security> findByCreditRating(Security.CreditRating creditRating);
    
    @Query("SELECT s FROM Security s WHERE s.securityName LIKE %:name%")
    List<Security> findBySecurityNameContaining(@Param("name") String name);
    
    @Query("SELECT s FROM Security s WHERE s.currentPrice BETWEEN :minPrice AND :maxPrice")
    List<Security> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT s FROM Security s WHERE s.marketCap BETWEEN :minCap AND :maxCap")
    List<Security> findByMarketCapRange(@Param("minCap") BigDecimal minCap, 
                                       @Param("maxCap") BigDecimal maxCap);
    
    @Query("SELECT COUNT(s) FROM Security s WHERE s.isActive = true")
    Long countActiveSecurities();
}
