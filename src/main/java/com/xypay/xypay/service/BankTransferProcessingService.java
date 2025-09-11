package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.User;

import com.xypay.xypay.repository.BankTransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Service for processing bank transfers using the signal handler components.
 */
@Service
public class BankTransferProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankTransferProcessingService.class);
    
    @Autowired
    private BankTransferRepository bankTransferRepository;
    
    @Autowired
    private BaseSignalHandler baseSignalHandler;
    
    @Autowired
    private BankTransferEventPublisher eventPublisher;
    
    /**
     * Create and process a bank transfer.
     *
     * @param user The user initiating the transfer
     * @param bankName The name of the destination bank
     * @param bankCode The code of the destination bank
     * @param accountNumber The destination account number
     * @param amount The transfer amount
     * @param description The transfer description
     * @return The created bank transfer
     */
    @Transactional
    public BankTransfer createAndProcessTransfer(User user, String bankName, String bankCode, 
            String accountNumber, BigDecimal amount, String description) {
        
        logger.info("Creating bank transfer for user ID: {}, amount: {}", user.getId(), amount);
        
        // Create the bank transfer
        BankTransfer transfer = new BankTransfer();
        transfer.setUser(user);
        transfer.setBankName(bankName);
        transfer.setBankCode(bankCode);
        transfer.setAccountNumber(accountNumber);
        transfer.setAmount(amount);
        transfer.setDescription(description);
        transfer.setReference("TRF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transfer.setStatus("PENDING");
        
        // Set default fee, vat, and levy
        transfer.setFee(BigDecimal.ZERO);
        transfer.setVat(BigDecimal.ZERO);
        transfer.setLevy(BigDecimal.ZERO);
        
        // Validate the transfer
        boolean isValid = baseSignalHandler.validateTransfer(transfer);
        if (!isValid) {
            logger.warn("Bank transfer validation failed for user ID: {}", user.getId());
            return transfer;
        }
        
        // Save the transfer
        transfer = bankTransferRepository.save(transfer);
        
        // Publish transfer created event
        eventPublisher.publishTransferCreatedEvent(transfer);
        
        // Simulate processing
        processTransfer(transfer);
        
        return transfer;
    }
    
    /**
     * Process a bank transfer.
     *
     * @param transfer The bank transfer to process
     */
    private void processTransfer(BankTransfer transfer) {
        logger.info("Processing bank transfer ID: {}", transfer.getId());
        
        // Simulate some processing logic
        try {
            // Update status to processing
            transfer.setStatus("processing");
            transfer.setProcessingStartedAt(java.time.LocalDateTime.now());
            bankTransferRepository.save(transfer);
            
            // Publish status update event
            eventPublisher.publishTransferStatusUpdatedEvent(transfer);
            
            // Simulate successful processing
            Thread.sleep(1000); // Simulate processing time
            
            // Update status to successful
            transfer.setStatus("successful");
            transfer.setProcessingCompletedAt(java.time.LocalDateTime.now());
            bankTransferRepository.save(transfer);
            
            // Process successful transfer
            baseSignalHandler.processSuccessfulTransfer(transfer);
            
            // Publish status update event
            eventPublisher.publishTransferStatusUpdatedEvent(transfer);
            
            logger.info("Successfully processed bank transfer ID: {}", transfer.getId());
        } catch (Exception e) {
            logger.error("Error processing bank transfer ID: {}: {}", transfer.getId(), e.getMessage());
            
            // Handle transfer failure
            baseSignalHandler.handleTransferFailure(transfer, StandardizedErrorCodes.PROCESSING_ERROR, e.getMessage());
            
            // Publish transfer failed event
            eventPublisher.publishTransferFailedEvent(transfer.getId(), StandardizedErrorCodes.PROCESSING_ERROR, e.getMessage());
        }
    }
    
    /**
     * Handle a failed bank transfer.
     *
     * @param transferId The ID of the failed transfer
     * @param errorCode The error code
     * @param errorMessage The error message
     */
    public void handleFailedTransfer(Long transferId, String errorCode, String errorMessage) {
        logger.info("Handling failed bank transfer ID: {}, error code: {}", transferId, errorCode);
        
        // Publish transfer failed event
        eventPublisher.publishTransferFailedEvent(transferId, errorCode, errorMessage);
    }
}