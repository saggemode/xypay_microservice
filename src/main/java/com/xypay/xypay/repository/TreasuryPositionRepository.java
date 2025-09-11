package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TreasuryPosition;
import com.xypay.xypay.domain.TreasuryOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TreasuryPositionRepository extends JpaRepository<TreasuryPosition, Long> {
    
    List<TreasuryPosition> findByTreasuryOperation(TreasuryOperation treasuryOperation);
    
    List<TreasuryPosition> findByPositionType(TreasuryPosition.PositionType positionType);
    
    List<TreasuryPosition> findByPositionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT tp FROM TreasuryPosition tp WHERE tp.positionValue BETWEEN :minValue AND :maxValue")
    List<TreasuryPosition> findByPositionValueRange(@Param("minValue") BigDecimal minValue, 
                                                   @Param("maxValue") BigDecimal maxValue);
    
    @Query("SELECT SUM(tp.positionValue) FROM TreasuryPosition tp WHERE tp.positionType = :positionType")
    BigDecimal getTotalPositionValueByType(@Param("positionType") TreasuryPosition.PositionType positionType);
    
    @Query("SELECT SUM(tp.unrealizedPnl) FROM TreasuryPosition tp")
    BigDecimal getTotalUnrealizedPnl();
}
