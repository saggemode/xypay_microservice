package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

/**
 * Base signal handler providing common functionality for transaction processing.
 */
@Component
public class BaseSignalHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseSignalHandler.class);
    
    @Autowired
    private WalletRepository walletRepository;
    
    /**
     * Standard validation for bank transfers.
     *
     * @param transfer The bank transfer to validate
     * @return true if validation passes, false otherwise
     */
    public boolean validateTransfer(BankTransfer transfer) {
        // Skip validation for bulk transfers during processing
        if (Boolean.TRUE.equals(transfer.getIsBulk()) && transfer.getBulkTransferId() != null) {
            return true;
        }
        
        // Validate transfer details
        StringBuilder errors = new StringBuilder();
        
        // Check for self-transfer
        if (transfer.getUser() != null) {
            List<Wallet> userWallets = walletRepository.findByUser(transfer.getUser());
            if (!userWallets.isEmpty()) {
                Wallet userWallet = userWallets.get(0); // Get the first wallet
                if (userWallet.getAccountNumber() != null && 
                    userWallet.getAccountNumber().equals(transfer.getAccountNumber())) {
                    errors.append("Cannot transfer to own account; ");
                    transfer.setStatus("failed");
                    transfer.setFailureReason(com.xypay.xypay.service.StandardizedErrorCodes.SELF_TRANSFER_ATTEMPT);
                    return false;
                }
            }
        }
        
        // Check amount is positive
        if (transfer.getAmount() != null && transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("Transfer amount must be positive; ");
            transfer.setStatus("failed");
            transfer.setFailureReason(com.xypay.xypay.service.StandardizedErrorCodes.VALIDATION_ERROR);
            return false;
        }
        
        return true;
    }
    
    /**
     * Process actions that should occur when a transfer is successful.
     *
     * @param transfer The successful bank transfer
     * @return true if processing succeeds, false otherwise
     */
    public boolean processSuccessfulTransfer(BankTransfer transfer) {
        try {
            // Update wallet balance
            if (transfer.getUser() != null) {
                List<Wallet> userWallets = walletRepository.findByUser(transfer.getUser());
                if (!userWallets.isEmpty()) {
                    Wallet wallet = userWallets.get(0); // Get the first wallet
                    BigDecimal totalDeduction = transfer.getAmount();
                    
                    if (transfer.getFee() != null) {
                        totalDeduction = totalDeduction.add(transfer.getFee());
                    }
                    if (transfer.getVat() != null) {
                        totalDeduction = totalDeduction.add(transfer.getVat());
                    }
                    if (transfer.getLevy() != null) {
                        totalDeduction = totalDeduction.add(transfer.getLevy());
                    }
                    
                    // In a real implementation, you would update the wallet balance here
                    logger.info("Would deduct {} from wallet {} for transfer {}", 
                        totalDeduction, wallet.getAccountNumber(), transfer.getId());
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error processing successful transfer {}: {}", transfer.getId(), e.getMessage());
            handleServiceError("processSuccessfulTransfer", e);
            return false;
        }
    }
    
    /**
     * Standardized handling of transfer failures.
     *
     * @param transfer The failed bank transfer
     * @param errorCode The error code
     * @param errorMessage The error message
     * @return true if handling succeeds, false otherwise
     */
    public boolean handleTransferFailure(BankTransfer transfer, String errorCode, String errorMessage) {
        try {
            transfer.setStatus("failed");
            transfer.setFailureReason(errorCode);
            
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Append to existing failure reason if it exists
                if (transfer.getFailureReason() != null && !transfer.getFailureReason().isEmpty()) {
                    transfer.setFailureReason(transfer.getFailureReason() + "; " + errorMessage);
                } else {
                    transfer.setFailureReason(errorMessage);
                }
            }
            
            // In a real implementation, you would save the transfer here
            logger.warn("Transfer {} failed: {} - {}", transfer.getId(), errorCode, errorMessage);
            return true;
        } catch (Exception e) {
            logger.error("Error handling transfer failure for {}: {}", transfer.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle service errors.
     *
     * @param operation The operation that failed
     * @param exception The exception that occurred
     */
    private void handleServiceError(String operation, Exception exception) {
        logger.error("Service error in operation {}: {}", operation, exception.getMessage());
        // In a real implementation, you might want to:
        // 1. Send alerts to monitoring systems
        // 2. Record the error in a database
        // 3. Trigger circuit breaker patterns
        // 4. Notify administrators
    }
}