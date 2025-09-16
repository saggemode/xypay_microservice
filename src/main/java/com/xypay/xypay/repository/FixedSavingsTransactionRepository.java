package com.xypay.xypay.repository;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.FixedSavingsTransaction;
import com.xypay.xypay.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FixedSavingsTransactionRepository extends JpaRepository<FixedSavingsTransaction, UUID> {
    
    List<FixedSavingsTransaction> findByFixedSavingsAccount(FixedSavingsAccount account);
    
    List<FixedSavingsTransaction> findByFixedSavingsAccountUser(User user);
    
    List<FixedSavingsTransaction> findByFixedSavingsAccountUserOrderByCreatedAtDesc(User user);
    
    List<FixedSavingsTransaction> findByFixedSavingsAccountUserAndTransactionType(User user, FixedSavingsTransaction.TransactionType transactionType);
    
    @Query("SELECT ft FROM FixedSavingsTransaction ft WHERE ft.fixedSavingsAccount.user = :user ORDER BY ft.createdAt DESC")
    List<FixedSavingsTransaction> findByFixedSavingsAccountUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);
}