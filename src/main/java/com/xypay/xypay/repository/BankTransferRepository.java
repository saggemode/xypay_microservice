package com.xypay.xypay.repository;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BankTransferRepository extends JpaRepository<BankTransfer, UUID> {
    
    List<BankTransfer> findByUser(User user);
    
    List<BankTransfer> findByUserAndStatus(User user, String status);
    
    List<BankTransfer> findByUserAndStatusIn(User user, List<String> statuses);
    
    List<BankTransfer> findByAccountNumber(String accountNumber);
    
    List<BankTransfer> findByReference(String reference);
}