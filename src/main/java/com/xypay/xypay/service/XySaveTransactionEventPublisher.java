package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.event.XySaveTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service to publish XySave transaction events.
 * Equivalent to Django's post_save signal publishing for XySaveTransaction.
 */
@Service
public class XySaveTransactionEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveTransactionEventPublisher.class);
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Publish event for XySave transaction creation.
     *
     * @param xySaveTransaction The created XySave transaction
     */
    public void publishXySaveTransactionCreatedEvent(XySaveTransaction xySaveTransaction) {
        logger.info("Publishing XySave transaction created event for transaction ID: {}", xySaveTransaction.getId());
        XySaveTransactionEvent event = new XySaveTransactionEvent(this, xySaveTransaction, true);
        eventPublisher.publishEvent(event);
    }
    
    /**
     * Publish event for XySave transaction update.
     *
     * @param xySaveTransaction The updated XySave transaction
     */
    public void publishXySaveTransactionUpdatedEvent(XySaveTransaction xySaveTransaction) {
        logger.info("Publishing XySave transaction updated event for transaction ID: {}", xySaveTransaction.getId());
        XySaveTransactionEvent event = new XySaveTransactionEvent(this, xySaveTransaction, false);
        eventPublisher.publishEvent(event);
    }
}
