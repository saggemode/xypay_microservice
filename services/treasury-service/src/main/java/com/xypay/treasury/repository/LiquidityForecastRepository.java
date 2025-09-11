package com.xypay.treasury.repository;

import com.xypay.treasury.domain.LiquidityForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LiquidityForecastRepository extends JpaRepository<LiquidityForecast, Long> {
    
    List<LiquidityForecast> findByCurrencyCodeOrderByForecastDateDesc(String currencyCode);
    
    List<LiquidityForecast> findByIsActiveTrue();
    
    List<LiquidityForecast> findByForecastDate(LocalDate forecastDate);
    
    List<LiquidityForecast> findByForecastDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT lf FROM LiquidityForecast lf WHERE lf.currencyCode = :currencyCode " +
           "AND lf.forecastDate = :forecastDate AND lf.isActive = true")
    LiquidityForecast findActiveForecastByCurrencyAndDate(
        @Param("currencyCode") String currencyCode, 
        @Param("forecastDate") LocalDate forecastDate);
    
    @Query("SELECT lf FROM LiquidityForecast lf WHERE lf.currencyCode = :currencyCode " +
           "AND lf.isActive = true ORDER BY lf.forecastDate DESC")
    List<LiquidityForecast> findLatestForecastsByCurrency(@Param("currencyCode") String currencyCode);
    
    @Query("SELECT DISTINCT lf.currencyCode FROM LiquidityForecast lf WHERE lf.isActive = true")
    List<String> findDistinctCurrencyCodes();
}
