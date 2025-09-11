package com.xypay.xypay.event;

import com.xypay.xypay.domain.XySaveTransaction;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a XySaveTransaction is created or updated.
 * Equivalent to Django's post_save signal for XySaveTransaction.
 */
public class XySaveTransactionEvent extends ApplicationEvent {
    
    private final XySaveTransaction xySaveTransaction;
    private final boolean isNewlyCreated;
    
    public XySaveTransactionEvent(Object source, XySaveTransaction xySaveTransaction, boolean isNewlyCreated) {
        super(source);
        this.xySaveTransaction = xySaveTransaction;
        this.isNewlyCreated = isNewlyCreated;
    }
    
    public XySaveTransaction getXySaveTransaction() {
        return xySaveTransaction;
    }
    
    public boolean isNewlyCreated() {
        return isNewlyCreated;
    }
    
    @Override
    public String toString() {
        return String.format("XySaveTransactionEvent{id=%s, type=%s, amount=%s, isNewlyCreated=%s}", 
            xySaveTransaction.getId(),
            xySaveTransaction.getTransactionType(),
            xySaveTransaction.getAmount(),
            isNewlyCreated);
    }
}
