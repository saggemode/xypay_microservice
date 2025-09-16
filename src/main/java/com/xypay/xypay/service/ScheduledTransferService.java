package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ScheduledTransferService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTransferService.class);
    
    @Autowired
    private ScheduledTransferRepository scheduledTransferRepository;
    
    @Autowired
    private BankTransferProcessingService bankTransferProcessingService;
    
    @Autowired
    private TransactionCreationService transactionCreationService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create a new scheduled transfer
     */
    public ScheduledTransfer createScheduledTransfer(ScheduledTransfer scheduledTransfer) {
        try {
            // Validate recipient account
            validateRecipientAccount(scheduledTransfer);
            
            // Set initial values
            scheduledTransfer.setNextRunDate(scheduledTransfer.getStartDate());
            scheduledTransfer.setStatus(ScheduledTransfer.Status.ACTIVE);
            
            ScheduledTransfer saved = scheduledTransferRepository.save(scheduledTransfer);
            
            logger.info("Created scheduled transfer {} for user {}", 
                saved.getId(), saved.getUser().getUsername());
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error creating scheduled transfer: {}", e.getMessage());
            throw new RuntimeException("Failed to create scheduled transfer", e);
        }
    }
    
    /**
     * Execute scheduled transfers (runs daily)
     */
    @Scheduled(cron = "0 0 6 * * ?") // Run at 6 AM daily
    public void executeScheduledTransfers() {
        logger.info("Starting scheduled transfer execution");
        
        try {
            List<ScheduledTransfer> dueTransfers = scheduledTransferRepository
                    .findByStatusAndNextRunDateLessThanEqual(ScheduledTransfer.Status.ACTIVE, LocalDate.now());
            
            logger.info("Found {} scheduled transfers due for execution", dueTransfers.size());
            
            for (ScheduledTransfer scheduledTransfer : dueTransfers) {
                try {
                    executeScheduledTransfer(scheduledTransfer);
                } catch (Exception e) {
                    logger.error("Error executing scheduled transfer {}: {}", 
                        scheduledTransfer.getId(), e.getMessage());
                }
            }
            
            logger.info("Completed scheduled transfer execution");
            
        } catch (Exception e) {
            logger.error("Error in scheduled transfer execution: {}", e.getMessage());
        }
    }
    
    /**
     * Execute individual scheduled transfer
     */
    public void executeScheduledTransfer(ScheduledTransfer scheduledTransfer) {
        try {
            scheduledTransfer.incrementRunCount();
            scheduledTransfer.setLastRunDate(LocalDate.now());
            
            // Check if transfer is expired
            if (scheduledTransfer.isExpired()) {
                scheduledTransfer.markAsCompleted();
                scheduledTransferRepository.save(scheduledTransfer);
                return;
            }
            
            // Execute the transfer
            boolean success = processScheduledTransfer(scheduledTransfer);
            
            if (success) {
                scheduledTransfer.incrementSuccessfulRuns();
                scheduledTransfer.resetRetryCount();
                scheduledTransfer.setStatus(ScheduledTransfer.Status.ACTIVE);
                
                // Update next run date
                scheduledTransfer.updateNextRunDate();
                
                // Send success notification
                notificationService.sendNotification(
                    scheduledTransfer.getUser().getId(),
                    "SCHEDULED_TRANSFER_SUCCESS",
                    String.format("Scheduled transfer '%s' executed successfully", scheduledTransfer.getTitle())
                );
                
            } else {
                scheduledTransfer.incrementFailedRuns();
                scheduledTransfer.markAsFailed("Transfer execution failed");
                
                // Check if should retry
                if (scheduledTransfer.shouldRetry()) {
                    scheduledTransfer.setStatus(ScheduledTransfer.Status.ACTIVE);
                    // Retry tomorrow
                    scheduledTransfer.setNextRunDate(LocalDate.now().plusDays(1));
                }
                
                // Send failure notification
                notificationService.sendNotification(
                    scheduledTransfer.getUser().getId(),
                    "SCHEDULED_TRANSFER_FAILED",
                    String.format("Scheduled transfer '%s' failed: %s", 
                        scheduledTransfer.getTitle(), scheduledTransfer.getLastFailureReason())
                );
            }
            
            scheduledTransferRepository.save(scheduledTransfer);
            
        } catch (Exception e) {
            logger.error("Error executing scheduled transfer {}: {}", 
                scheduledTransfer.getId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Process the actual transfer
     */
    private boolean processScheduledTransfer(ScheduledTransfer scheduledTransfer) {
        try {
            if (scheduledTransfer.getIsInternalTransfer()) {
                return processInternalScheduledTransfer(scheduledTransfer);
            } else {
                return processExternalScheduledTransfer(scheduledTransfer);
            }
        } catch (Exception e) {
            logger.error("Error processing scheduled transfer {}: {}", 
                scheduledTransfer.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Process internal scheduled transfer
     */
    private boolean processInternalScheduledTransfer(ScheduledTransfer scheduledTransfer) {
        try {
            Wallet recipientWallet = walletService.getWalletByAnyAccountNumber(
                scheduledTransfer.getRecipientAccountNumber()).orElse(null);
            
            if (recipientWallet == null) {
                throw new RuntimeException("Recipient wallet not found");
            }
            
            // Create internal transfer
            transactionCreationService
                    .createTransactionRecords(
                        scheduledTransfer.getUser().getWallet(),
                        recipientWallet,
                        scheduledTransfer.getAmount(),
                        null, // No BankTransfer for internal transfers
                        "Scheduled transfer: " + scheduledTransfer.getDescription()
                    );
            
            return true;
            
        } catch (Exception e) {
            logger.error("Internal scheduled transfer failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Process external scheduled transfer
     */
    private boolean processExternalScheduledTransfer(ScheduledTransfer scheduledTransfer) {
        try {
            // Create external bank transfer
            BankTransfer bankTransfer = new BankTransfer();
            bankTransfer.setUser(scheduledTransfer.getUser());
            bankTransfer.setAccountNumber(scheduledTransfer.getRecipientAccountNumber());
            bankTransfer.setBankName("Unknown Bank"); // Set bank name instead
            bankTransfer.setBankCode(scheduledTransfer.getRecipientBankCode());
            bankTransfer.setAmount(scheduledTransfer.getAmount());
            bankTransfer.setDescription(scheduledTransfer.getDescription());
            bankTransfer.setReference("SCH-" + scheduledTransfer.getId() + "-" + System.currentTimeMillis());
            bankTransfer.setStatus("PENDING");
            
            bankTransferProcessingService.createAndProcessTransfer(
                scheduledTransfer.getUser(),
                "Unknown Bank", // Default bank name
                scheduledTransfer.getRecipientBankCode(),
                scheduledTransfer.getRecipientAccountNumber(),
                scheduledTransfer.getAmount(),
                scheduledTransfer.getDescription()
            );
            
            return true;
            
        } catch (Exception e) {
            logger.error("External scheduled transfer failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate recipient account
     */
    private void validateRecipientAccount(ScheduledTransfer scheduledTransfer) {
        // Check if it's an internal account
        Wallet recipientWallet = walletService.getWalletByAnyAccountNumber(
            scheduledTransfer.getRecipientAccountNumber()).orElse(null);
        
        if (recipientWallet != null) {
            scheduledTransfer.setIsInternalTransfer(true);
            String fullName = recipientWallet.getUser().getFirstName() + " " + recipientWallet.getUser().getLastName();
            scheduledTransfer.setRecipientName(fullName);
        } else {
            scheduledTransfer.setIsInternalTransfer(false);
            // For external accounts, we would validate with NIBSS
            // This is a placeholder for external validation
        }
    }
    
    /**
     * Get scheduled transfers by user
     */
    public List<ScheduledTransfer> getScheduledTransfersByUser(User user) {
        return scheduledTransferRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get active scheduled transfers
     */
    public List<ScheduledTransfer> getActiveScheduledTransfers() {
        return scheduledTransferRepository.findByStatus(ScheduledTransfer.Status.ACTIVE);
    }
    
    /**
     * Pause scheduled transfer
     */
    public ScheduledTransfer pauseScheduledTransfer(UUID id) {
        ScheduledTransfer scheduledTransfer = scheduledTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled transfer not found"));
        
        scheduledTransfer.pause();
        return scheduledTransferRepository.save(scheduledTransfer);
    }
    
    /**
     * Resume scheduled transfer
     */
    public ScheduledTransfer resumeScheduledTransfer(UUID id) {
        ScheduledTransfer scheduledTransfer = scheduledTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled transfer not found"));
        
        scheduledTransfer.resume();
        return scheduledTransferRepository.save(scheduledTransfer);
    }
    
    /**
     * Cancel scheduled transfer
     */
    public ScheduledTransfer cancelScheduledTransfer(UUID id) {
        ScheduledTransfer scheduledTransfer = scheduledTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled transfer not found"));
        
        scheduledTransfer.cancel();
        return scheduledTransferRepository.save(scheduledTransfer);
    }
    
    /**
     * Get scheduled transfer by ID
     */
    public ScheduledTransfer getScheduledTransfer(UUID id) {
        return scheduledTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled transfer not found"));
    }
}
