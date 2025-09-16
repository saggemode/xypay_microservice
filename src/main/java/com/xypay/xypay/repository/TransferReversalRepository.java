package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TransferReversal;
import com.xypay.xypay.domain.TransferStatus;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransferReversalRepository extends JpaRepository<TransferReversal, UUID> {
    
    List<TransferReversal> findByInitiatedByOrderByCreatedAtDesc(User initiatedBy);
    
    List<TransferReversal> findByStatus(TransferStatus status);
    
    List<TransferReversal> findByApprovedByOrderByProcessedAtDesc(User approvedBy);
    
    List<TransferReversal> findByReason(TransferReversal.ReversalReason reason);
    
    List<TransferReversal> findByStatusAndProcessedAtIsNull(TransferStatus status);
    
    List<TransferReversal> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT tr FROM TransferReversal tr WHERE tr.status = :status AND tr.processedAt IS NULL")
    List<TransferReversal> findPendingReversals(@Param("status") TransferStatus status);
    
    @Query("SELECT tr FROM TransferReversal tr WHERE tr.initiatedBy = :user AND tr.status = :status")
    List<TransferReversal> findByUserAndStatus(@Param("user") User user, @Param("status") TransferStatus status);
    
    @Query("SELECT COUNT(tr) FROM TransferReversal tr WHERE tr.status = :status")
    long countByStatus(@Param("status") TransferStatus status);
    
    @Query("SELECT tr FROM TransferReversal tr WHERE tr.originalTransfer.id = :originalTransferId")
    Optional<TransferReversal> findByOriginalTransferId(@Param("originalTransferId") UUID originalTransferId);
    
    @Query("SELECT CASE WHEN COUNT(tr) > 0 THEN true ELSE false END FROM TransferReversal tr WHERE tr.originalTransfer.id = :originalTransferId")
    boolean existsByOriginalTransferId(@Param("originalTransferId") UUID originalTransferId);
}
