package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransferReversalService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransferReversalService.class);
    
    @Autowired
    private TransferReversalRepository transferReversalRepository;
    
    @Autowired
    private BankTransferRepository bankTransferRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create a new transfer reversal request
     */
    public TransferReversal createTransferReversal(BankTransfer originalTransfer, User initiatedBy, 
                                                  String reason, TransferReversal.ReversalReason reversalReason) {
        try {
            // Validate reversal eligibility
            validateReversalEligibility(originalTransfer);
            
            // Create reversal record
            TransferReversal reversal = new TransferReversal();
            reversal.setOriginalTransfer(originalTransfer);
            reversal.setInitiatedBy(initiatedBy);
            reversal.setAmount(originalTransfer.getAmount());
            reversal.setReason(reversalReason);
            reversal.setDescription(reason);
            reversal.setStatus(TransferStatus.PENDING);
            
            TransferReversal saved = transferReversalRepository.save(reversal);
            
            // Send notifications
            notificationService.sendNotification(
                initiatedBy.getId(),
                "REVERSAL_REQUESTED",
                String.format("Transfer reversal request %s created", saved.getId())
            );
            
            logger.info("Created transfer reversal {} for transfer {}", 
                saved.getId(), originalTransfer.getId());
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error creating transfer reversal: {}", e.getMessage());
            throw new RuntimeException("Failed to create transfer reversal", e);
        }
    }
    
    /**
     * Approve transfer reversal
     */
    public TransferReversal approveReversal(UUID reversalId, User approver, String approvalNotes) {
        try {
            TransferReversal reversal = transferReversalRepository.findById(reversalId)
                    .orElseThrow(() -> new RuntimeException("Transfer reversal not found"));
            
            reversal.setApprovedBy(approver);
            reversal.setStatus(TransferStatus.APPROVED);
            reversal = transferReversalRepository.save(reversal);
            
            // Process the reversal
            processTransferReversal(reversal);
            
            // Send notifications
            notificationService.sendNotification(
                reversal.getInitiatedBy().getId(),
                "REVERSAL_APPROVED",
                String.format("Transfer reversal %s has been approved", reversal.getId())
            );
            
            logger.info("Approved transfer reversal {}", reversal.getId());
            
            return reversal;
            
        } catch (Exception e) {
            logger.error("Error approving transfer reversal {}: {}", reversalId, e.getMessage());
            throw new RuntimeException("Failed to approve transfer reversal", e);
        }
    }
    
    /**
     * Process transfer reversal
     */
    public TransferReversal processTransferReversal(TransferReversal reversal) {
        try {
            reversal.setStatus(TransferStatus.PROCESSING);
            transferReversalRepository.save(reversal);
            
            BankTransfer originalTransfer = reversal.getOriginalTransfer();
            
            // Create reversal transfer
            BankTransfer reversalTransfer = createReversalTransfer(originalTransfer, reversal);
            reversal.setReversalTransfer(reversalTransfer);
            
            // Update original transfer status
            originalTransfer.setStatus("reversed");
            
            // Complete reversal
            reversal.setStatus(TransferStatus.COMPLETED);
            reversal.setProcessedAt(LocalDateTime.now());
            reversal = transferReversalRepository.save(reversal);
            
            // Send notifications
            notificationService.sendNotification(
                originalTransfer.getUser().getId(),
                "TRANSFER_REVERSED",
                String.format("Transfer %s has been reversed. Amount: %s", 
                    originalTransfer.getReference(), reversal.getAmount())
            );
            
            logger.info("Processed transfer reversal {} for transfer {}", 
                reversal.getId(), originalTransfer.getId());
            
            return reversal;
            
        } catch (Exception e) {
            reversal.setStatus(TransferStatus.FAILED);
            transferReversalRepository.save(reversal);
            
            logger.error("Error processing transfer reversal {}: {}", 
                reversal.getId(), e.getMessage());
            throw new RuntimeException("Failed to process transfer reversal", e);
        }
    }
    
    /**
     * Create reversal transfer
     */
    private BankTransfer createReversalTransfer(BankTransfer originalTransfer, TransferReversal reversal) {
        BankTransfer reversalTransfer = new BankTransfer();
        reversalTransfer.setUser(originalTransfer.getUser());
        reversalTransfer.setBankName(originalTransfer.getBankName());
        reversalTransfer.setBankCode(originalTransfer.getBankCode());
        reversalTransfer.setAccountNumber(originalTransfer.getAccountNumber());
        reversalTransfer.setAmount(originalTransfer.getAmount());
        reversalTransfer.setReference("REV-" + originalTransfer.getReference());
        reversalTransfer.setStatus("completed");
        reversalTransfer.setTransferType(originalTransfer.getTransferType());
        reversalTransfer.setDescription("Reversal: " + originalTransfer.getDescription());
        
        return bankTransferRepository.save(reversalTransfer);
    }
    
    /**
     * Validate reversal eligibility
     */
    private void validateReversalEligibility(BankTransfer transfer) {
        // Check if transfer is already reversed
        if ("reversed".equals(transfer.getStatus())) {
            throw new RuntimeException("Transfer is already reversed");
        }
        
        // Check if transfer is successful
        if (!"completed".equals(transfer.getStatus()) && !"successful".equals(transfer.getStatus())) {
            throw new RuntimeException("Only successful transfers can be reversed");
        }
        
        // Check time limit (24 hours for customer requests)
        LocalDateTime transferTime = transfer.getCreatedAt();
        LocalDateTime timeLimit = transferTime.plusHours(24);
        
        if (LocalDateTime.now().isAfter(timeLimit)) {
            throw new RuntimeException("Transfer is too old to be reversed (24 hour limit)");
        }
    }
    
    /**
     * Get transfer reversals by user
     */
    public List<TransferReversal> getTransferReversalsByUser(User user) {
        return transferReversalRepository.findByInitiatedByOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get pending reversals requiring approval
     */
    public List<TransferReversal> getPendingReversals() {
        return transferReversalRepository.findByStatus(TransferStatus.PENDING);
    }
    
    /**
     * Get transfer reversal by ID
     */
    public TransferReversal getTransferReversal(UUID id) {
        return transferReversalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer reversal not found"));
    }
    
    /**
     * Cancel transfer reversal
     */
    public TransferReversal cancelTransferReversal(UUID id, User user) {
        TransferReversal reversal = getTransferReversal(id);
        
        if (!reversal.getInitiatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Only the initiator can cancel this reversal");
        }
        
        if (reversal.getStatus() == TransferStatus.PROCESSING || reversal.getStatus() == TransferStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel reversal that is processing or completed");
        }
        
        reversal.setStatus(TransferStatus.CANCELLED);
        return transferReversalRepository.save(reversal);
    }
    
    /**
     * Get reversal statistics
     */
    public ReversalStatistics getReversalStatistics() {
        List<TransferReversal> allReversals = transferReversalRepository.findAll();
        
        ReversalStatistics stats = new ReversalStatistics();
        stats.setTotalReversals(allReversals.size());
        stats.setPendingReversals((int) allReversals.stream().filter(r -> r.getStatus() == TransferStatus.PENDING).count());
        stats.setCompletedReversals((int) allReversals.stream().filter(r -> r.getStatus() == TransferStatus.COMPLETED).count());
        stats.setFailedReversals((int) allReversals.stream().filter(r -> r.getStatus() == TransferStatus.FAILED).count());
        
        return stats;
    }
    
    // Inner class for statistics
    public static class ReversalStatistics {
        private int totalReversals;
        private int pendingReversals;
        private int completedReversals;
        private int failedReversals;
        
        // Getters and setters
        public int getTotalReversals() { return totalReversals; }
        public void setTotalReversals(int totalReversals) { this.totalReversals = totalReversals; }
        
        public int getPendingReversals() { return pendingReversals; }
        public void setPendingReversals(int pendingReversals) { this.pendingReversals = pendingReversals; }
        
        public int getCompletedReversals() { return completedReversals; }
        public void setCompletedReversals(int completedReversals) { this.completedReversals = completedReversals; }
        
        public int getFailedReversals() { return failedReversals; }
        public void setFailedReversals(int failedReversals) { this.failedReversals = failedReversals; }
    }
}
