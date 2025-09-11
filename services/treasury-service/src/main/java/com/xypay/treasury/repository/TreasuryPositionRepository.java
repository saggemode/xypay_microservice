package com.xypay.treasury.repository;

import com.xypay.treasury.domain.TreasuryPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TreasuryPositionRepository extends JpaRepository<TreasuryPosition, Long> {
    
    List<TreasuryPosition> findByCurrencyCodeAndIsActiveTrue(String currencyCode);
    
    List<TreasuryPosition> findByIsActiveTrue();
    
    List<TreasuryPosition> findByCurrencyCode(String currencyCode);
    
    List<TreasuryPosition> findByPositionType(TreasuryPosition.PositionType positionType);
    
    List<TreasuryPosition> findByLiquidityBucket(TreasuryPosition.LiquidityBucket liquidityBucket);
    
    List<TreasuryPosition> findByValueDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<TreasuryPosition> findByMaturityDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<TreasuryPosition> findByCostCenter(String costCenter);
    
    List<TreasuryPosition> findByProfitCenter(String profitCenter);
    
    @Query("SELECT DISTINCT tp.currencyCode FROM TreasuryPosition tp WHERE tp.isActive = true")
    List<String> findDistinctCurrencyCodes();
    
    @Query("SELECT SUM(tp.positionAmount) FROM TreasuryPosition tp WHERE tp.currencyCode = :currencyCode AND tp.isActive = true")
    BigDecimal getTotalPositionAmountByCurrency(@Param("currencyCode") String currencyCode);
    
    @Query("SELECT SUM(tp.availableAmount) FROM TreasuryPosition tp WHERE tp.currencyCode = :currencyCode AND tp.isActive = true")
    BigDecimal getTotalAvailableAmountByCurrency(@Param("currencyCode") String currencyCode);
    
    @Query("SELECT SUM(tp.reservedAmount) FROM TreasuryPosition tp WHERE tp.currencyCode = :currencyCode AND tp.isActive = true")
    BigDecimal getTotalReservedAmountByCurrency(@Param("currencyCode") String currencyCode);
    
    @Query("SELECT tp FROM TreasuryPosition tp WHERE tp.currencyCode = :currencyCode " +
           "AND tp.liquidityBucket = :liquidityBucket AND tp.isActive = true")
    List<TreasuryPosition> findByCurrencyAndLiquidityBucket(
        @Param("currencyCode") String currencyCode, 
        @Param("liquidityBucket") TreasuryPosition.LiquidityBucket liquidityBucket);
    
    @Query("SELECT tp FROM TreasuryPosition tp WHERE tp.maturityDate <= :date AND tp.isActive = true")
    List<TreasuryPosition> findMaturedPositions(@Param("date") LocalDate date);
    
    @Query("SELECT tp FROM TreasuryPosition tp WHERE tp.maturityDate BETWEEN :startDate AND :endDate AND tp.isActive = true")
    List<TreasuryPosition> findPositionsMaturingBetween(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
}
