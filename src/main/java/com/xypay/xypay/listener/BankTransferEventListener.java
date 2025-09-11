package com.xypay.xypay.listener;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.enums.TransferErrorCodes;
import com.xypay.xypay.event.BankTransferEvent;
import com.xypay.xypay.repository.WalletRepository;
import com.xypay.xypay.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Event listener for bank transfer processing.
 * Equivalent to Django's @receiver(post_save, sender=BankTransfer) signal handler.
 */
@Component("listenerBankTransferEventListener")
public class BankTransferEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(BankTransferEventListener.class);
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private SecurityGuardService securityGuardService;
    
    @Autowired
    private TransactionCreationService transactionCreationService;
    
    @Autowired
    private TransferNotificationService transferNotificationService;
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    @Autowired
    private com.xypay.xypay.repository.BankTransferRepository bankTransferRepository;
    
    // TODO: Add XySave services when implemented
    // @Autowired
    // private XySaveAccountService xySaveAccountService;
    // @Autowired
    // private XySaveTransactionService xySaveTransactionService;
    
    /**
     * Handle bank transfer processing with comprehensive failure tracking.
     * Equivalent to Django's handle_bank_transfer function.
     */
    @EventListener
    @Async
    @Transactional
    public void handleBankTransfer(BankTransferEvent event) {
        logger.info("🔔 BankTransferEvent received! Transfer ID: {}, isNewlyCreated: {}", 
            event.getBankTransfer().getId(), event.isNewlyCreated());
        
        BankTransfer instance = event.getBankTransfer();
        
        if (!event.isNewlyCreated() || !"PENDING".equalsIgnoreCase(instance.getStatus())) {
            logger.info("⏭️ Skipping transfer {} - isNewlyCreated: {}, status: {}", 
                instance.getId(), event.isNewlyCreated(), instance.getStatus());
            return;
        }
        
        logger.info("🚀 Processing bank transfer {} - amount: {}, account: {}", 
            instance.getId(), instance.getAmount(), instance.getAccountNumber());
        
        try {
            // Security checks
            if (!passesSecurityChecks(instance)) {
                return; // Security checks failed, transfer is on hold
            }
            
            // Find sender wallet
            List<Wallet> senderWallets = walletRepository.findByUser(instance.getUser());
            if (senderWallets.isEmpty()) {
                logger.error("Sender wallet not found for user {}", instance.getUser().getId());
                markTransferAsFailed(instance, "Sender wallet not found", 
                    TransferErrorCodes.WALLET_NOT_FOUND, createTechnicalDetails(instance, "user_id", instance.getUser().getId()));
                return;
            }
            
            Wallet senderWallet = senderWallets.get(0); // Get the first wallet
            logger.info("Found sender wallet: {}, balance: {}", senderWallet.getAccountNumber(), senderWallet.getBalance());
            
            // TODO: Implement XySave prefunding logic
            boolean prefundedFromXySave = attemptXySavePrefunding(instance, senderWallet);
            
            // Check for self-transfer
            if (instance.getAccountNumber().equals(senderWallet.getAccountNumber())) {
                logger.warn("Self-transfer attempt detected for user {}", instance.getUser().getId());
                Map<String, Object> details = new HashMap<>();
                details.put("sender_account", senderWallet.getAccountNumber());
                details.put("recipient_account", instance.getAccountNumber());
                details.put("user_id", instance.getUser().getId());
                markTransferAsFailed(instance, "Self-transfer is not allowed", 
                    TransferErrorCodes.SELF_TRANSFER_ATTEMPT, details);
                return;
            }
            
            // Check sufficient balance
            if (senderWallet.getBalance().compareTo(instance.getAmount()) < 0) {
                logger.warn("Insufficient funds in sender wallet {}. Balance: {}, Required: {}", 
                    senderWallet.getAccountNumber(), senderWallet.getBalance(), instance.getAmount());
                Map<String, Object> details = createInsufficientFundsDetails(senderWallet, instance);
                markTransferAsFailed(instance, "Insufficient funds in sender wallet", 
                    TransferErrorCodes.INSUFFICIENT_FUNDS, details);
                return;
            }
            
            // Find receiver wallet (internal transfer)
            Optional<Wallet> receiverWalletOpt = findReceiverWallet(instance.getAccountNumber());
            
            if (receiverWalletOpt.isPresent()) {
                processInternalTransfer(instance, senderWallet, receiverWalletOpt.get(), prefundedFromXySave);
            } else {
                processExternalTransfer(instance);
            }
            
        } catch (Exception e) {
            logger.error("Error processing bank transfer {}: {}", instance.getId(), e.getMessage());
            Map<String, Object> details = createErrorDetails(e, instance);
            markTransferAsFailed(instance, "Processing error: " + e.getMessage(), 
                TransferErrorCodes.PROCESSING_ERROR, details);
        }
    }
    
    private boolean passesSecurityChecks(BankTransfer instance) {
        // Night Guard check
        Map<String, Object> nightGuard = securityGuardService.applyNightGuard(instance);
        if ((Boolean) nightGuard.get("required")) {
            String status = getMetadataValue(instance, "night_guard_status");
            if (!securityGuardService.isSecurityVerificationPassed(status)) {
                logger.info("Night Guard active for transfer {}; awaiting verification. status={}", 
                    instance.getId(), status);
                return false;
            }
        }
        
        // Large Transaction Shield check
        Map<String, Object> largeTransactionShield = securityGuardService.applyLargeTransactionShield(instance);
        if ((Boolean) largeTransactionShield.get("required")) {
            String status = getMetadataValue(instance, "large_tx_shield_status");
            if (!securityGuardService.isSecurityVerificationPassed(status)) {
                logger.info("Large Transaction Shield active for transfer {}; awaiting verification. status={}", 
                    instance.getId(), status);
                return false;
            }
        }
        
        // Location Guard check
        Map<String, Object> locationGuard = securityGuardService.applyLocationGuard(instance);
        if ((Boolean) locationGuard.get("required")) {
            String status = getMetadataValue(instance, "location_guard_status");
            if (!securityGuardService.isSecurityVerificationPassed(status)) {
                logger.info("Location Guard active for transfer {}; awaiting verification. status={}", 
                    instance.getId(), status);
                return false;
            }
        }
        
        return true;
    }
    
    private boolean attemptXySavePrefunding(BankTransfer instance, Wallet senderWallet) {
        // TODO: Implement XySave prefunding logic
        logger.info("XySave prefunding not yet implemented for transfer {}", instance.getId());
        return false;
    }
    
    private Optional<Wallet> findReceiverWallet(String accountNumber) {
        // Check both primary and alternative account numbers
        return walletRepository.findByAccountNumberOrAlternativeAccountNumber(accountNumber, accountNumber);
    }
    
    private void processInternalTransfer(BankTransfer instance, Wallet senderWallet, 
                                       Wallet receiverWallet, boolean prefundedFromXySave) {
        try {
            String accountType = instance.getAccountNumber().equals(receiverWallet.getAccountNumber()) ? "primary" : "alternative";
            logger.info("Found internal receiver by {} account: {}", accountType, instance.getAccountNumber());
            
            // Deduct from sender
            senderWallet.setBalance(senderWallet.getBalance().subtract(instance.getAmount()));
            walletRepository.save(senderWallet);
            
            // Add to receiver
            receiverWallet.setBalance(receiverWallet.getBalance().add(instance.getAmount()));
            walletRepository.save(receiverWallet);
            
            // Create transaction records
            String description = "Transfer to " + receiverWallet.getAccountNumber();
            TransactionCreationService.TransactionPair transactions = transactionCreationService
                .createTransactionRecords(senderWallet, receiverWallet, instance.getAmount(), instance, description);
            
            // Mark XySave prefunding if applicable
            if (prefundedFromXySave) {
                markTransactionAsPrefundedFromXySave(transactions.getSenderTransaction());
            }
            
            // Send notifications
            transferNotificationService.sendTransferNotifications(senderWallet, receiverWallet, 
                instance.getAmount(), instance);
            
            // Mark as successful
            instance.setStatus("successful");
            instance.setProcessingCompletedAt(LocalDateTime.now());
            bankTransferRepository.save(instance);
            
            logger.info("Internal transfer completed successfully: {}", instance.getId());
            logger.info("Created transactions - Sender: {}, Receiver: {}", 
                transactions.getSenderTransaction().getId(), transactions.getReceiverTransaction().getId());
            
        } catch (Exception e) {
            logger.error("Error processing internal transfer {}: {}", instance.getId(), e.getMessage());
            Map<String, Object> details = createErrorDetails(e, instance);
            markTransferAsFailed(instance, "Processing error: " + e.getMessage(), 
                TransferErrorCodes.PROCESSING_ERROR, details);
        }
    }
    
    private void processExternalTransfer(BankTransfer instance) {
        // External transfer: record debit transaction immediately, notifications, mark successful
        logger.info("External transfer initiated: {}", instance.getId());
        try {
            // Find sender wallet again to ensure latest balance
            List<Wallet> senderWallets = walletRepository.findByUser(instance.getUser());
            if (senderWallets.isEmpty()) {
                logger.error("Sender wallet not found during external transfer {}", instance.getId());
                markTransferAsFailed(instance, "Sender wallet not found",
                    TransferErrorCodes.WALLET_NOT_FOUND, createTechnicalDetails(instance, "user_id", instance.getUser().getId()));
                return;
            }
            Wallet senderWallet = senderWallets.get(0);

            // Deduct from sender
            senderWallet.setBalance(senderWallet.getBalance().subtract(instance.getAmount()));
            walletRepository.save(senderWallet);

            // Create single sender transaction (debit)
            String description = "External transfer to " + instance.getBankName() + " / " + instance.getAccountNumber();
            transactionCreationService.createExternalSenderTransaction(senderWallet, instance.getAmount(), instance, description);

            // Send notifications (receiverWallet is null for external; notify sender only via service method with null receiver handled upstream)
            try {
                transferNotificationService.sendTransferNotifications(senderWallet, senderWallet, instance.getAmount(), instance);
            } catch (Exception e) {
                logger.warn("Notification sending failed for external transfer {}: {}", instance.getId(), e.getMessage());
            }

            // Mark as successful for demo completeness
            instance.setStatus("successful");
            instance.setProcessingCompletedAt(LocalDateTime.now());
            bankTransferRepository.save(instance);
            logger.info("External transfer completed (demo mode) {}", instance.getId());
        } catch (Exception e) {
            logger.error("Error processing external transfer {}: {}", instance.getId(), e.getMessage());
            Map<String, Object> details = createErrorDetails(e, instance);
            markTransferAsFailed(instance, "Processing error: " + e.getMessage(),
                TransferErrorCodes.PROCESSING_ERROR, details);
        }
    }
    
    private void markTransactionAsPrefundedFromXySave(Transaction transaction) {
        try {
            // Metadata is stored as JSON string, not Map
            String currentMetadata = transaction.getMetadata();
            if (currentMetadata == null || currentMetadata.isEmpty()) {
                currentMetadata = "{}";
            }
            // Simple JSON update - in real implementation, use proper JSON library
            String updatedMetadata = currentMetadata.replace("}", ",\"prefunded_from_xysave\":true}");
            if (updatedMetadata.equals("{,\"prefunded_from_xysave\":true}")) {
                updatedMetadata = "{\"prefunded_from_xysave\":true}";
            }
            transaction.setMetadata(updatedMetadata);
            // Note: In real implementation, save through service
        } catch (Exception e) {
            logger.warn("Failed to annotate transaction {} with XySave prefund flag: {}", 
                transaction.getId(), e.getMessage());
        }
    }
    
    private void markTransferAsFailed(BankTransfer instance, String reason, 
                                    TransferErrorCodes errorCode, Map<String, Object> technicalDetails) {
        logger.error("Transfer {} failed: {} ({})", instance.getId(), reason, errorCode);
        
        // Update transfer status to failed
        instance.setStatus("failed");
        instance.setFailureReason(reason);
        instance.setProcessingCompletedAt(LocalDateTime.now());
        
        // Save the failed transfer
        bankTransferRepository.save(instance);
        
        // Log audit event
        auditTrailService.logEvent("TRANSFER_FAILED", 
            String.format("Transfer %s failed: %s", instance.getId(), reason));
    }
    
    private String getMetadataValue(BankTransfer instance, String key) {
        String metadata = instance.getMetadata();
        if (metadata != null && !metadata.isEmpty()) {
            // Simple JSON parsing - in real implementation, use proper JSON library
            String searchPattern = "\"" + key + "\":\"";
            int startIndex = metadata.indexOf(searchPattern);
            if (startIndex != -1) {
                startIndex += searchPattern.length();
                int endIndex = metadata.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return metadata.substring(startIndex, endIndex);
                }
            }
        }
        return null;
    }
    
    private Map<String, Object> createTechnicalDetails(BankTransfer instance, String key, Object value) {
        Map<String, Object> details = new HashMap<>();
        details.put(key, value);
        details.put("transfer_id", instance.getId().toString());
        return details;
    }
    
    private Map<String, Object> createInsufficientFundsDetails(Wallet senderWallet, BankTransfer instance) {
        Map<String, Object> details = new HashMap<>();
        details.put("sender_account", senderWallet.getAccountNumber());
        details.put("available_balance", senderWallet.getBalance());
        details.put("required_amount", instance.getAmount());
        details.put("shortfall", instance.getAmount().subtract(senderWallet.getBalance()));
        return details;
    }
    
    private Map<String, Object> createErrorDetails(Exception e, BankTransfer instance) {
        Map<String, Object> details = new HashMap<>();
        details.put("error_type", e.getClass().getSimpleName());
        details.put("error_message", e.getMessage());
        details.put("transfer_id", instance.getId().toString());
        details.put("user_id", instance.getUser().getId());
        details.put("amount", instance.getAmount());
        details.put("account_number", instance.getAccountNumber());
        return details;
    }
}
