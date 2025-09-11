package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SecurityTransaction;
import com.xypay.xypay.domain.Portfolio;
import com.xypay.xypay.domain.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityTransactionRepository extends JpaRepository<SecurityTransaction, Long> {
    
    List<SecurityTransaction> findByPortfolio(Portfolio portfolio);
    
    List<SecurityTransaction> findByPortfolioIdOrderByTradeDateDesc(Long portfolioId);
    
    List<SecurityTransaction> findBySecurity(Security security);
    
    List<SecurityTransaction> findByTransactionType(SecurityTransaction.TransactionType transactionType);
    
    List<SecurityTransaction> findByStatus(SecurityTransaction.TransactionStatus status);
    
    List<SecurityTransaction> findBySettlementStatus(SecurityTransaction.SettlementStatus settlementStatus);
    
    List<SecurityTransaction> findByComplianceStatus(SecurityTransaction.ComplianceStatus complianceStatus);
    
    List<SecurityTransaction> findByTradeDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<SecurityTransaction> findBySettlementDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<SecurityTransaction> findByCounterparty(String counterparty);
    
    List<SecurityTransaction> findByBroker(String broker);
    
    List<SecurityTransaction> findByExchange(String exchange);
    
    List<SecurityTransaction> findByTraderId(Long traderId);
    
    @Query("SELECT st FROM SecurityTransaction st WHERE st.netAmount BETWEEN :minAmount AND :maxAmount")
    List<SecurityTransaction> findByNetAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                                 @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT COUNT(st) FROM SecurityTransaction st WHERE st.status = :status")
    Long countByStatus(@Param("status") SecurityTransaction.TransactionStatus status);
    
    @Query("SELECT SUM(st.netAmount) FROM SecurityTransaction st WHERE st.transactionType = 'BUY' AND st.status = 'FILLED'")
    BigDecimal getTotalBuyVolume();
    
    @Query("SELECT SUM(st.netAmount) FROM SecurityTransaction st WHERE st.transactionType = 'SELL' AND st.status = 'FILLED'")
    BigDecimal getTotalSellVolume();
    
    @Query("SELECT SUM(st.realizedPnl) FROM SecurityTransaction st WHERE st.transactionType = 'SELL' AND st.status = 'FILLED'")
    BigDecimal getTotalRealizedPnl();
    
    @Query("SELECT st FROM SecurityTransaction st WHERE st.approvalWorkflowId = :workflowId")
    SecurityTransaction findByApprovalWorkflowId(@Param("workflowId") Long workflowId);
}
