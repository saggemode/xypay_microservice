package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.SpendAndSaveTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpendAndSaveTransactionRepository extends JpaRepository<SpendAndSaveTransaction, UUID> {
    List<SpendAndSaveTransaction> findBySpendAndSaveAccountOrderByCreatedAtDesc(SpendAndSaveAccount account, int limit);
    List<SpendAndSaveTransaction> findBySpendAndSaveAccount(SpendAndSaveAccount account);
    List<SpendAndSaveTransaction> findBySpendAndSaveAccountAndCreatedAtBetween(SpendAndSaveAccount account, LocalDateTime startDate, LocalDateTime endDate);
    List<SpendAndSaveTransaction> findBySpendAndSaveAccountAndTransactionTypeAndCreatedAtAfter(SpendAndSaveAccount account, SpendAndSaveTransaction.TransactionType transactionType, LocalDateTime startDate);
}