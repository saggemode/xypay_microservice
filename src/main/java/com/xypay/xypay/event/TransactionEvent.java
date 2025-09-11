package com.xypay.xypay.event;

import com.xypay.xypay.domain.Transaction;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a Transaction is created or updated.
 * Equivalent to Django's post_save signal for Transaction.
 */
public class TransactionEvent extends ApplicationEvent {
    
    private final Transaction transaction;
    private final boolean isNewlyCreated;
    
    public TransactionEvent(Object source, Transaction transaction, boolean isNewlyCreated) {
        super(source);
        this.transaction = transaction;
        this.isNewlyCreated = isNewlyCreated;
    }
    
    public Transaction getTransaction() {
        return transaction;
    }
    
    public boolean isNewlyCreated() {
        return isNewlyCreated;
    }
    
    @Override
    public String toString() {
        return String.format("TransactionEvent{id=%s, type=%s, amount=%s, status=%s, isNewlyCreated=%s}", 
            transaction.getId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getStatus(),
            isNewlyCreated);
    }
}
