package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TransferReversal;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferReversalRepository extends JpaRepository<TransferReversal, Long> {
    
    List<TransferReversal> findByInitiatedByOrderByCreatedAtDesc(User initiatedBy);
    
    List<TransferReversal> findByStatus(TransferReversal.Status status);
    
    List<TransferReversal> findByStatusAndApprovalRequiredTrue(TransferReversal.Status status);
    
    List<TransferReversal> findByApprovedByOrderByApprovedAtDesc(User approvedBy);
    
    List<TransferReversal> findByReversalType(TransferReversal.ReversalType reversalType);
    
    TransferReversal findByReversalId(String reversalId);
    
    boolean existsByOriginalTransactionId(Long originalTransactionId);
}
