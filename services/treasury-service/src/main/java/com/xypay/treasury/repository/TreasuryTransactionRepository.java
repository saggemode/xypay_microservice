package com.xypay.treasury.repository;

import com.xypay.treasury.domain.TreasuryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TreasuryTransactionRepository extends JpaRepository<TreasuryTransaction, Long> {
    
    List<TreasuryTransaction> findByCurrencyCodeOrderByValueDateDesc(String currencyCode);
    
    List<TreasuryTransaction> findByStatus(String status);
    
    List<TreasuryTransaction> findByTransactionType(String transactionType);
    
    List<TreasuryTransaction> findByTransactionCategory(TreasuryTransaction.TransactionCategory category);
    
    List<TreasuryTransaction> findByValueDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TreasuryTransaction> findBySettlementDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TreasuryTransaction> findByCounterparty(String counterparty);
    
    List<TreasuryTransaction> findByReference(String reference);
    
    @Query("SELECT SUM(tt.amount) FROM TreasuryTransaction tt WHERE tt.currencyCode = :currencyCode " +
           "AND tt.status = 'COMPLETED' AND tt.valueDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByCurrencyAndDateRange(
        @Param("currencyCode") String currencyCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT tt FROM TreasuryTransaction tt WHERE tt.currencyCode = :currencyCode " +
           "AND tt.transactionType = :transactionType AND tt.status = 'COMPLETED' " +
           "AND tt.valueDate BETWEEN :startDate AND :endDate")
    List<TreasuryTransaction> findTransactionsByCurrencyTypeAndDateRange(
        @Param("currencyCode") String currencyCode,
        @Param("transactionType") String transactionType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}
