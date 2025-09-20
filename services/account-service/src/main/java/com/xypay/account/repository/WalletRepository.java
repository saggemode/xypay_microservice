package com.xypay.account.repository;

import com.xypay.account.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    
    List<Wallet> findByUserId(UUID userId);
    
    List<Wallet> findByStatus(String status);
    
    Wallet findByAccountNumber(String accountNumber);
    
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.status = :status")
    List<Wallet> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);
    
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.userId = :userId")
    Long countByUserId(@Param("userId") UUID userId);
}
