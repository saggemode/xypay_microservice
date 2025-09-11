package com.xypay.xypay.service;

import com.xypay.xypay.event.TransactionEvent;
import com.xypay.xypay.event.BankTransferEvent;
import com.xypay.xypay.service.CustomerEscalationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for handling various notification events.
 */
@Component
public class NotificationEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Handle transaction creation events.
     * This method is called after the transaction is committed.
     */
    @EventListener
    public void handleTransactionEvent(TransactionEvent event) {
        logger.info("Handling transaction event for transaction ID: {}", event.getTransaction().getId());
        notificationService.handleTransactionNotifications(event.getTransaction());
    }
    
    /**
     * Handle customer escalation events.
     */
    @EventListener
    public void handleCustomerEscalationEvent(CustomerEscalationEvent event) {
        logger.info("Handling customer escalation event for escalation ID: {}", event.getEscalation().getId());
        notificationService.handleEscalationStatusChange(event.getEscalation(), event.isCreated());
    }
    
    /**
     * Handle bank transfer events.
     */
    @EventListener
    public void handleBankTransferEvent(BankTransferEvent event) {
        logger.info("Handling bank transfer event for transfer ID: {}", event.getBankTransfer().getId());
        notificationService.handleBankTransferNotifications(event.getBankTransfer(), event.isNewlyCreated());
    }
}