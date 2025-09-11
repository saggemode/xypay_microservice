package com.xypay.account.repository;

import com.xypay.account.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Optional<Wallet> findByUserId(Long userId);
    
    Optional<Wallet> findByAccountNumber(String accountNumber);
    
    Optional<Wallet> findByAlternativeAccountNumber(String alternativeAccountNumber);
    
    Optional<Wallet> findByPhoneAlias(String phoneAlias);
    
    @Query("SELECT w FROM Wallet w WHERE w.isActive = true")
    List<Wallet> findActiveWallets();
    
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.isActive = true")
    Optional<Wallet> findActiveWalletByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Wallet w WHERE w.branchId = :branchId")
    List<Wallet> findByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT w FROM Wallet w WHERE w.currency = :currency")
    List<Wallet> findByCurrency(@Param("currency") String currency);
    
    @Query("SELECT w FROM Wallet w WHERE w.balance > :minBalance")
    List<Wallet> findByMinBalance(@Param("minBalance") java.math.BigDecimal minBalance);
}
