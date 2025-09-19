package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.SpendAndSaveTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpendAndSaveTransactionRepository extends JpaRepository<SpendAndSaveTransaction, UUID> {
    
    @Query("SELECT t FROM SpendAndSaveTransaction t WHERE t.spendAndSaveAccount = :account ORDER BY t.createdAt DESC LIMIT :limit")
    List<SpendAndSaveTransaction> findBySpendAndSaveAccountOrderByCreatedAtDesc(@Param("account") SpendAndSaveAccount account, @Param("limit") int limit);
    
    @Query("SELECT t FROM SpendAndSaveTransaction t WHERE t.spendAndSaveAccount = :account")
    List<SpendAndSaveTransaction> findBySpendAndSaveAccount(@Param("account") SpendAndSaveAccount account);
    
    @Query("SELECT t FROM SpendAndSaveTransaction t WHERE t.spendAndSaveAccount = :account AND t.createdAt BETWEEN :startDate AND :endDate")
    List<SpendAndSaveTransaction> findBySpendAndSaveAccountAndCreatedAtBetween(@Param("account") SpendAndSaveAccount account, 
                                                                               @Param("startDate") LocalDateTime startDate, 
                                                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM SpendAndSaveTransaction t WHERE t.spendAndSaveAccount = :account AND t.transactionType = :transactionType AND t.createdAt > :startDate")
    List<SpendAndSaveTransaction> findBySpendAndSaveAccountAndTransactionTypeAndCreatedAtAfter(@Param("account") SpendAndSaveAccount account, 
                                                                                               @Param("transactionType") SpendAndSaveTransaction.TransactionType transactionType, 
                                                                                               @Param("startDate") LocalDateTime startDate);
}