package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TransactionApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionApprovalRepository extends JpaRepository<TransactionApproval, UUID> {
    
    List<TransactionApproval> findByStatusOrderByCreatedAtDesc(TransactionApproval.ApprovalStatus status);
    
    List<TransactionApproval> findByRequestedByOrderByCreatedAtDesc(UUID requestedById);
    
    List<TransactionApproval> findByApprovedByOrderByCreatedAtDesc(UUID approvedById);
    
    List<TransactionApproval> findByEscalatedToOrderByCreatedAtDesc(UUID escalatedToId);
    
    @Query("SELECT ta FROM TransactionApproval ta WHERE ta.transaction.id = :transactionId")
    List<TransactionApproval> findByTransactionId(@Param("transactionId") UUID transactionId);
    
    @Query("SELECT ta FROM TransactionApproval ta WHERE ta.status = 'PENDING' AND ta.requestedBy.role.level >= :minLevel")
    List<TransactionApproval> findPendingApprovalsByMinLevel(@Param("minLevel") Integer minLevel);
}
