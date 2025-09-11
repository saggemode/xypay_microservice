package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.event.BankTransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


/**
 * Service to publish bank transfer events.
 */
@Service
public class BankTransferEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(BankTransferEventPublisher.class);
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Publish event for bank transfer creation.
     *
     * @param transfer The created bank transfer
     */
    public void publishTransferCreatedEvent(BankTransfer transfer) {
        logger.info("📢 Publishing bank transfer created event for transfer ID: {}", transfer.getId());
        BankTransferEvent event = new BankTransferEvent(this, transfer, true);
        eventPublisher.publishEvent(event);
        logger.info("✅ Bank transfer event published successfully for transfer ID: {}", transfer.getId());
    }
    
    /**
     * Publish event for bank transfer status update.
     *
     * @param transfer The updated bank transfer
     */
    public void publishTransferStatusUpdatedEvent(BankTransfer transfer) {
        logger.info("Publishing bank transfer status updated event for transfer ID: {}", transfer.getId());
        BankTransferEvent event = new BankTransferEvent(this, transfer, false);
        eventPublisher.publishEvent(event);
    }
    
    /**
     * Publish event for bank transfer failure.
     *
     * @param transferId The ID of the failed transfer
     * @param errorCode The error code
     * @param errorMessage The error message
     */
    public void publishTransferFailedEvent(Long transferId, String errorCode, String errorMessage) {
        logger.info("Publishing bank transfer failed event for transfer ID: {}, error code: {}", 
            transferId, errorCode);
        BankTransferFailedEvent event = new BankTransferFailedEvent(transferId, errorCode, errorMessage);
        eventPublisher.publishEvent(event);
    }
}