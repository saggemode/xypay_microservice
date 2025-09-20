package com.xypay.account.repository;

import com.xypay.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    List<Account> findByCustomerId(UUID customerId);
    
    List<Account> findByStatus(String status);
    
    Account findByAccountNumber(String accountNumber);
    
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.status = :status")
    List<Account> findByCustomerIdAndStatus(@Param("customerId") UUID customerId, @Param("status") String status);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT a FROM Account a WHERE a.accountType = :accountType AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByType(@Param("accountType") String accountType);
}
