package com.xypay.xypay.repository;

import com.xypay.xypay.domain.EscrowAccount;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, Long> {
    
    List<EscrowAccount> findByBuyerOrSellerOrderByCreatedAtDesc(User buyer, User seller);
    
    List<EscrowAccount> findByStatus(EscrowAccount.Status status);
    
    List<EscrowAccount> findByStatusAndExpiryDateBefore(EscrowAccount.Status status, LocalDateTime expiryDate);
    
    List<EscrowAccount> findByBuyerOrderByCreatedAtDesc(User buyer);
    
    List<EscrowAccount> findBySellerOrderByCreatedAtDesc(User seller);
    
    EscrowAccount findByEscrowId(String escrowId);
    
    @Query("SELECT e FROM EscrowAccount e WHERE e.buyer = :user OR e.seller = :user ORDER BY e.createdAt DESC")
    List<EscrowAccount> findUserEscrowAccounts(User user);
    
    @Query("SELECT e FROM EscrowAccount e WHERE e.status = 'FUNDED' AND e.expiryDate < :now")
    List<EscrowAccount> findExpiredFundedEscrowAccounts(LocalDateTime now);
}
