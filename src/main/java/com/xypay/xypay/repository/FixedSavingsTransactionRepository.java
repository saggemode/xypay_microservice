package com.xypay.xypay.repository;

import com.xypay.xypay.domain.FixedSavingsTransaction;
import com.xypay.xypay.domain.FixedSavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FixedSavingsTransactionRepository extends JpaRepository<FixedSavingsTransaction, Long> {
    List<FixedSavingsTransaction> findByFixedSavingsAccountOrderByCreatedAtDesc(FixedSavingsAccount account);
    List<FixedSavingsTransaction> findByFixedSavingsAccountId(Long accountId);
}