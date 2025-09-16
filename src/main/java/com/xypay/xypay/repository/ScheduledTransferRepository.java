package com.xypay.xypay.repository;

import com.xypay.xypay.domain.ScheduledTransfer;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, UUID> {
    
    List<ScheduledTransfer> findByUserOrderByCreatedAtDesc(User user);
    
    List<ScheduledTransfer> findByStatus(ScheduledTransfer.Status status);
    
    List<ScheduledTransfer> findByStatusAndNextRunDateLessThanEqual(
        ScheduledTransfer.Status status, LocalDate nextRunDate);
    
    List<ScheduledTransfer> findByUserAndStatus(User user, ScheduledTransfer.Status status);
    
    List<ScheduledTransfer> findByRecipientAccountNumber(String accountNumber);
}
