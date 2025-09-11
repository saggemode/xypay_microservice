package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransferReversalService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransferReversalService.class);
    
    @Autowired
    private TransferReversalRepository transferReversalRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private BankTransferRepository bankTransferRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionCreationService transactionCreationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new transfer reversal request
     */
    public TransferReversal createTransferReversal(Transaction originalTransaction, User initiatedBy, 
                                                  String reason, TransferReversal.ReversalType reversalType) {
        try {
            // Validate reversal eligibility
            validateReversalEligibility(originalTransaction);
            
            // Create reversal record
            TransferReversal reversal = new TransferReversal(
                originalTransaction, 
                initiatedBy, 
                originalTransaction.getAmount(), 
                reversalType, 
                reason
            );
            
            // Check if approval is required
            if (requiresApproval(originalTransaction.getAmount(), reversalType)) {
                reversal.setApprovalRequired(true);
            }
            
            TransferReversal saved = transferReversalRepository.save(reversal);
            
            // Send notifications
            notificationService.sendNotification(
                initiatedBy.getId(),
                "REVERSAL_REQUESTED",
                String.format("Transfer reversal request %s created", saved.getReversalId())
            );
            
            logger.info("Created transfer reversal {} for transaction {}", 
                saved.getReversalId(), originalTransaction.getId());
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error creating transfer reversal: {}", e.getMessage());
            throw new RuntimeException("Failed to create transfer reversal", e);
        }
    }
    
    /**
     * Approve transfer reversal
     */
    public TransferReversal approveReversal(Long reversalId, User approver, String approvalNotes) {
        try {
            TransferReversal reversal = transferReversalRepository.findById(reversalId)
                    .orElseThrow(() -> new RuntimeException("Transfer reversal not found"));
            
            if (!reversal.requiresApproval()) {
                throw new RuntimeException("This reversal does not require approval");
            }
            
            reversal.approve(approver, approvalNotes);
            reversal = transferReversalRepository.save(reversal);
            
            // Process the reversal
            processTransferReversal(reversal);
            
            // Send notifications
            notificationService.sendNotification(
                reversal.getInitiatedBy().getId(),
                "REVERSAL_APPROVED",
                String.format("Transfer reversal %s has been approved", reversal.getReversalId())
            );
            
            logger.info("Approved transfer reversal {}", reversal.getReversalId());
            
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
            reversal.startProcessing();
            transferReversalRepository.save(reversal);
            
            Transaction originalTransaction = reversal.getOriginalTransaction();
            
            // Create reversal transaction
            Transaction reversalTransaction = createReversalTransaction(originalTransaction, reversal);
            
            // Update wallet balances
            updateWalletBalances(originalTransaction, reversalTransaction);
            
            // Update original transaction status
            originalTransaction.setStatus("REVERSED");
            transactionRepository.save(originalTransaction);
            
            // Complete reversal
            reversal.completeProcessing(reversalTransaction);
            reversal = transferReversalRepository.save(reversal);
            
            // Send notifications
            notificationService.sendNotification(
                originalTransaction.getWallet().getUser().getId(),
                "TRANSFER_REVERSED",
                String.format("Transfer %s has been reversed. Amount: %s", 
                    originalTransaction.getReference(), reversal.getAmount())
            );
            
            logger.info("Processed transfer reversal {} for transaction {}", 
                reversal.getReversalId(), originalTransaction.getId());
            
            return reversal;
            
        } catch (Exception e) {
            reversal.markAsFailed(e.getMessage());
            transferReversalRepository.save(reversal);
            
            logger.error("Error processing transfer reversal {}: {}", 
                reversal.getId(), e.getMessage());
            throw new RuntimeException("Failed to process transfer reversal", e);
        }
    }
    
    /**
     * Create reversal transaction
     */
    private Transaction createReversalTransaction(Transaction originalTransaction, TransferReversal reversal) {
        Transaction reversalTransaction = new Transaction();
        reversalTransaction.setWallet(originalTransaction.getWallet());
        reversalTransaction.setAmount(reversal.getAmount());
        reversalTransaction.setType(originalTransaction.getType().equals("DEBIT") ? "CREDIT" : "DEBIT");
        reversalTransaction.setChannel("REVERSAL");
        reversalTransaction.setDescription("Reversal: " + originalTransaction.getDescription());
        reversalTransaction.setReference("REV-" + originalTransaction.getReference());
        reversalTransaction.setStatus("SUCCESS");
        reversalTransaction.setBalanceAfter(originalTransaction.getWallet().getBalance());
        
        return transactionRepository.save(reversalTransaction);
    }
    
    /**
     * Update wallet balances
     */
    private void updateWalletBalances(Transaction originalTransaction, Transaction reversalTransaction) {
        Wallet wallet = originalTransaction.getWallet();
        
        if (originalTransaction.getType().equals("DEBIT")) {
            // Original was debit, reversal is credit (add money back)
            wallet.setBalance(wallet.getBalance().add(reversalTransaction.getAmount()));
        } else {
            // Original was credit, reversal is debit (remove money)
            wallet.setBalance(wallet.getBalance().subtract(reversalTransaction.getAmount()));
        }
        
        walletRepository.save(wallet);
        reversalTransaction.setBalanceAfter(wallet.getBalance());
        transactionRepository.save(reversalTransaction);
    }
    
    /**
     * Validate reversal eligibility
     */
    private void validateReversalEligibility(Transaction transaction) {
        // Check if transaction is already reversed
        if ("REVERSED".equals(transaction.getStatus())) {
            throw new RuntimeException("Transaction is already reversed");
        }
        
        // Check if transaction is successful
        if (!"SUCCESS".equals(transaction.getStatus())) {
            throw new RuntimeException("Only successful transactions can be reversed");
        }
        
        // Check time limit (24 hours for customer requests)
        LocalDateTime transactionTime = transaction.getCreatedAt();
        LocalDateTime timeLimit = transactionTime.plusHours(24);
        
        if (LocalDateTime.now().isAfter(timeLimit)) {
            throw new RuntimeException("Transaction is too old to be reversed (24 hour limit)");
        }
    }
    
    /**
     * Check if reversal requires approval
     */
    private boolean requiresApproval(BigDecimal amount, TransferReversal.ReversalType reversalType) {
        // High-value reversals require approval
        BigDecimal highValueThreshold = new BigDecimal("100000.00"); // ₦100,000
        
        if (amount.compareTo(highValueThreshold) > 0) {
            return true;
        }
        
        // Bank-initiated and compliance reversals require approval
        if (reversalType == TransferReversal.ReversalType.BANK_INITIATED || 
            reversalType == TransferReversal.ReversalType.COMPLIANCE) {
            return true;
        }
        
        return false;
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
        return transferReversalRepository.findByStatusAndApprovalRequiredTrue(TransferReversal.Status.PENDING);
    }
    
    /**
     * Get transfer reversal by ID
     */
    public TransferReversal getTransferReversal(Long id) {
        return transferReversalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer reversal not found"));
    }
    
    /**
     * Cancel transfer reversal
     */
    public TransferReversal cancelTransferReversal(Long id, User user) {
        TransferReversal reversal = getTransferReversal(id);
        
        if (!reversal.getInitiatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Only the initiator can cancel this reversal");
        }
        
        if (reversal.isProcessing() || reversal.isCompleted()) {
            throw new RuntimeException("Cannot cancel reversal that is processing or completed");
        }
        
        reversal.cancel();
        return transferReversalRepository.save(reversal);
    }
    
    /**
     * Get reversal statistics
     */
    public ReversalStatistics getReversalStatistics() {
        List<TransferReversal> allReversals = transferReversalRepository.findAll();
        
        ReversalStatistics stats = new ReversalStatistics();
        stats.setTotalReversals(allReversals.size());
        stats.setPendingReversals((int) allReversals.stream().filter(TransferReversal::isPending).count());
        stats.setCompletedReversals((int) allReversals.stream().filter(TransferReversal::isCompleted).count());
        stats.setFailedReversals((int) allReversals.stream().filter(TransferReversal::isFailed).count());
        
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
