package com.xypay.analytics.repository;

import com.xypay.analytics.domain.TransactionAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionAnalyticsRepository extends JpaRepository<TransactionAnalytics, Long> {
    
    Optional<TransactionAnalytics> findByTransactionId(Long transactionId);
    
    List<TransactionAnalytics> findByTransactionIdIn(List<Long> transactionIds);
    
    List<TransactionAnalytics> findByCurrency(String currency);
    
    List<TransactionAnalytics> findByTransactionType(String transactionType);
    
    List<TransactionAnalytics> findByChannel(String channel);
    
    @Query("SELECT ta FROM TransactionAnalytics ta WHERE ta.patternAnomalyScore > :threshold")
    List<TransactionAnalytics> findByHighAnomalyScore(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT ta FROM TransactionAnalytics ta WHERE ta.transactionVelocity > :threshold")
    List<TransactionAnalytics> findByHighVelocity(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT ta FROM TransactionAnalytics ta WHERE ta.processedAt BETWEEN :startDate AND :endDate")
    List<TransactionAnalytics> findByProcessedDateRange(@Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(ta.amount) FROM TransactionAnalytics ta")
    BigDecimal getAverageTransactionAmount();
    
    @Query("SELECT SUM(ta.amount) FROM TransactionAnalytics ta WHERE ta.currency = :currency")
    BigDecimal getTotalAmountByCurrency(@Param("currency") String currency);
    
    @Query("SELECT COUNT(ta) FROM TransactionAnalytics ta WHERE ta.transactionType = :transactionType")
    Long countByTransactionType(@Param("transactionType") String transactionType);
    
    @Query("SELECT ta FROM TransactionAnalytics ta WHERE ta.amount >= :minAmount AND ta.amount <= :maxAmount")
    List<TransactionAnalytics> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                                @Param("maxAmount") BigDecimal maxAmount);
}
