package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    
    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);    
    
    List<Transaction> findByWalletAndChannelAndTimestampBetweenAndStatus(
            Wallet wallet, String channel, LocalDateTime startDate, LocalDateTime endDate, String status);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :from AND :to")
    Page<Transaction> findByCreatedAtBetween(
        @Param("from") LocalDateTime from, 
        @Param("to") LocalDateTime to, 
        Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :from AND :to")
    List<Transaction> findByCreatedAtBetween(
        @Param("from") LocalDateTime from, 
        @Param("to") LocalDateTime to);
    
    // Additional methods for admin interface
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    long countByStatus(String status);
    
    long countByType(String type);
    
    long countByChannel(String channel);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end AND t.status = 'SUCCESS'")
    BigDecimal sumAmountByStatusAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    List<Transaction> findByWalletUserAndTypeAndCreatedAtAfter(User user, String type, LocalDateTime startDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalVolumeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'FAILED'")
    long countFailedTransactions();
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end")
    long countTransactionsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    List<Transaction> findByParent(Transaction parent);
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:channel IS NULL OR t.channel = :channel) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR t.reference LIKE CONCAT('%', :search, '%') OR t.description LIKE CONCAT('%', :search, '%')) AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<Transaction> findWithFilters(
        @Param("type") String type,
        @Param("channel") String channel,
        @Param("status") String status,
        @Param("search") String search,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:channel IS NULL OR t.channel = :channel) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:currency IS NULL OR t.currency = :currency) AND " +
           "(:search IS NULL OR t.reference LIKE CONCAT('%', :search, '%') OR t.description LIKE CONCAT('%', :search, '%'))")
    Page<Transaction> findWithAllFilters(
        @Param("type") String type,
        @Param("channel") String channel,
        @Param("status") String status,
        @Param("currency") String currency,
        @Param("search") String search,
        Pageable pageable
    );
}