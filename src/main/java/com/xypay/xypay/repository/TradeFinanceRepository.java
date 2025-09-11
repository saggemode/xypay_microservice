package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TradeFinance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeFinanceRepository extends JpaRepository<TradeFinance, Long> {
    
    List<TradeFinance> findByCustomerId(Long customerId);
    
    List<TradeFinance> findByBankId(Long bankId);
    
    List<TradeFinance> findByStatus(TradeFinance.TradeStatus status);
    
    List<TradeFinance> findByInstrumentType(TradeFinance.InstrumentType instrumentType);
    
    List<TradeFinance> findByTradeType(TradeFinance.TradeType tradeType);
    
    List<TradeFinance> findByExpiryDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TradeFinance> findByExpiryDateBeforeAndStatusIn(LocalDateTime date, List<TradeFinance.TradeStatus> statuses);
    
    List<TradeFinance> findByShariaCompliant(Boolean shariaCompliant);
    
    List<TradeFinance> findByIslamicStructure(TradeFinance.IslamicStructure structure);
    
    List<TradeFinance> findBySanctionsScreeningStatus(TradeFinance.ScreeningStatus status);
    
    List<TradeFinance> findByAmlScreeningStatus(TradeFinance.ScreeningStatus status);
    
    @Query("SELECT tf FROM TradeFinance tf WHERE tf.amount BETWEEN :minAmount AND :maxAmount")
    List<TradeFinance> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                       @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT COUNT(tf) FROM TradeFinance tf WHERE tf.status IN :statuses")
    Long countByStatusIn(@Param("statuses") List<TradeFinance.TradeStatus> statuses);
    
    @Query("SELECT SUM(tf.amount) FROM TradeFinance tf WHERE tf.status IN ('ISSUED', 'ADVISED')")
    BigDecimal getTotalExposure();
    
    @Query("SELECT tf FROM TradeFinance tf WHERE tf.approvalWorkflowId = :workflowId")
    TradeFinance findByApprovalWorkflowId(@Param("workflowId") Long workflowId);
    
    Long countByStatus(TradeFinance.TradeStatus status);
}
