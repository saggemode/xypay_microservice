package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TreasurySettlement;
import com.xypay.xypay.domain.TreasuryOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TreasurySettlementRepository extends JpaRepository<TreasurySettlement, UUID> {
    
    List<TreasurySettlement> findByTreasuryOperation(TreasuryOperation treasuryOperation);
    
    List<TreasurySettlement> findBySettlementStatus(TreasurySettlement.SettlementStatus status);
    
    List<TreasurySettlement> findBySettlementMethod(TreasurySettlement.SettlementMethod method);
    
    List<TreasurySettlement> findBySettlementDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TreasurySettlement> findBySettlementDateBeforeAndSettlementStatus(LocalDateTime date, 
                                                                           TreasurySettlement.SettlementStatus status);
    
    @Query("SELECT ts FROM TreasurySettlement ts WHERE ts.settlementAmount BETWEEN :minAmount AND :maxAmount")
    List<TreasurySettlement> findBySettlementAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                                        @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT COUNT(ts) FROM TreasurySettlement ts WHERE ts.settlementStatus = :status")
    Long countBySettlementStatus(@Param("status") TreasurySettlement.SettlementStatus status);
    
    @Query("SELECT SUM(ts.settlementAmount) FROM TreasurySettlement ts WHERE ts.settlementStatus = 'SETTLED'")
    BigDecimal getTotalSettledAmount();
}
