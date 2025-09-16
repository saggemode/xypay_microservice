package com.xypay.xypay.repository;

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
public interface TreasuryOperationRepository extends JpaRepository<TreasuryOperation, UUID> {
    
    List<TreasuryOperation> findByBankId(Long bankId);
    
    List<TreasuryOperation> findByOperationType(TreasuryOperation.OperationType operationType);
    
    List<TreasuryOperation> findByInstrumentType(TreasuryOperation.InstrumentType instrumentType);
    
    List<TreasuryOperation> findByStatus(TreasuryOperation.OperationStatus status);
    
    List<TreasuryOperation> findByStatusIn(List<TreasuryOperation.OperationStatus> statuses);
    
    List<TreasuryOperation> findByCounterparty(String counterparty);
    
    List<TreasuryOperation> findByCounterpartyRating(TreasuryOperation.CreditRating rating);
    
    List<TreasuryOperation> findByMaturityDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TreasuryOperation> findByMaturityDateBeforeAndStatusIn(LocalDateTime date, 
                                                               List<TreasuryOperation.OperationStatus> statuses);
    
    List<TreasuryOperation> findByTradeDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TreasuryOperation> findByCurrencyCode(String currencyCode);
    
    List<TreasuryOperation> findByShariaCompliant(Boolean shariaCompliant);
    
    List<TreasuryOperation> findByIslamicStructure(TreasuryOperation.IslamicStructure structure);
    
    List<TreasuryOperation> findByBaselClassification(TreasuryOperation.BaselClassification classification);
    
    List<TreasuryOperation> findByIfrsClassification(TreasuryOperation.IfrsClassification classification);
    
    @Query("SELECT to FROM TreasuryOperation to WHERE to.amount BETWEEN :minAmount AND :maxAmount")
    List<TreasuryOperation> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                            @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT COUNT(to) FROM TreasuryOperation to WHERE to.status = :status")
    Long countByStatus(@Param("status") TreasuryOperation.OperationStatus status);
    
    @Query("SELECT SUM(to.amount) FROM TreasuryOperation to WHERE to.status IN :statuses")
    BigDecimal getTotalNotionalByStatuses(@Param("statuses") List<TreasuryOperation.OperationStatus> statuses);
    
    @Query("SELECT SUM(to.marketValue) FROM TreasuryOperation to WHERE to.status = 'EXECUTED'")
    BigDecimal getTotalMarketValue();
    
    @Query("SELECT SUM(to.unrealizedPnl) FROM TreasuryOperation to WHERE to.status = 'EXECUTED'")
    BigDecimal getTotalUnrealizedPnl();
    
    @Query("SELECT SUM(to.varAmount) FROM TreasuryOperation to WHERE to.status = 'EXECUTED'")
    BigDecimal getTotalVaR();
    
    @Query("SELECT to FROM TreasuryOperation to WHERE to.approvalWorkflowId = :workflowId")
    TreasuryOperation findByApprovalWorkflowId(@Param("workflowId") Long workflowId);
    
    @Query("SELECT to FROM TreasuryOperation to WHERE to.operationType = :type AND to.status IN :statuses")
    List<TreasuryOperation> findByOperationTypeAndStatusIn(@Param("type") TreasuryOperation.OperationType type,
                                                          @Param("statuses") List<TreasuryOperation.OperationStatus> statuses);
}
