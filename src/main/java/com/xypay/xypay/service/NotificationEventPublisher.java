package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.CustomerEscalation;
import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.event.BankTransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service to publish notification events.
 */
@Service
public class NotificationEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventPublisher.class);
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Publish event for transaction creation.
     *
     * @param transaction The created transaction
     */
    public void publishTransactionCreatedEvent(Transaction transaction) {
        logger.info("Publishing transaction created event for transaction ID: {}", transaction.getId());
        eventPublisher.publishEvent(transaction);
    }
    
    /**
     * Publish event for customer escalation.
     *
     * @param escalation The customer escalation
     * @param created Whether the escalation was created or updated
     */
    public void publishCustomerEscalationEvent(CustomerEscalation escalation, boolean created) {
        logger.info("Publishing customer escalation event for escalation ID: {}, created: {}", 
            escalation.getId(), created);
        CustomerEscalationEvent event = new CustomerEscalationEvent(escalation, created);
        eventPublisher.publishEvent(event);
    }
    
    /**
     * Publish event for bank transfer.
     *
     * @param transfer The bank transfer
     * @param created Whether the transfer was created or updated
     */
    public void publishBankTransferEvent(BankTransfer transfer, boolean created) {
        logger.info("Publishing bank transfer event for transfer ID: {}, created: {}", 
            transfer.getId(), created);
        BankTransferEvent event = new BankTransferEvent(this, transfer, created);
        eventPublisher.publishEvent(event);
    }
}