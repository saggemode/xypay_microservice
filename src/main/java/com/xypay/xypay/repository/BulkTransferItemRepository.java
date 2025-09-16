package com.xypay.xypay.repository;

import com.xypay.xypay.domain.BulkTransfer;
import com.xypay.xypay.domain.BulkTransferItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BulkTransferItemRepository extends JpaRepository<BulkTransferItem, UUID> {
    
    List<BulkTransferItem> findByBulkTransfer(BulkTransfer bulkTransfer);
    
    List<BulkTransferItem> findByBulkTransferAndStatus(BulkTransfer bulkTransfer, BulkTransferItem.Status status);
    
    List<BulkTransferItem> findByRecipientAccountNumber(String accountNumber);
}
