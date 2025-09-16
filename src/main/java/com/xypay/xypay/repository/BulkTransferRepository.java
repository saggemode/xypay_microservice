package com.xypay.xypay.repository;

import com.xypay.xypay.domain.BulkTransfer;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BulkTransferRepository extends JpaRepository<BulkTransfer, UUID> {
    
    List<BulkTransfer> findByUserOrderByCreatedAtDesc(User user);
    
    List<BulkTransfer> findByStatusOrderByCreatedAtDesc(BulkTransfer.Status status);
    
    BulkTransfer findByBatchId(String batchId);
    
    boolean existsByBatchId(String batchId);
}
