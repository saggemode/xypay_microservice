package com.xypay.xypay.event;

import com.xypay.xypay.domain.BankTransfer;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a BankTransfer is created or updated.
 * Equivalent to Django's post_save signal for BankTransfer.
 */
public class BankTransferEvent extends ApplicationEvent {
    
    private final BankTransfer bankTransfer;
    private final boolean isNewlyCreated;
    
    public BankTransferEvent(Object source, BankTransfer bankTransfer, boolean isNewlyCreated) {
        super(source);
        this.bankTransfer = bankTransfer;
        this.isNewlyCreated = isNewlyCreated;
    }
    
    public BankTransfer getBankTransfer() {
        return bankTransfer;
    }
    
    public boolean isNewlyCreated() {
        return isNewlyCreated;
    }
    
    @Override
    public String toString() {
        return String.format("BankTransferEvent{id=%s, amount=%s, status=%s, isNewlyCreated=%s}", 
            bankTransfer.getId(),
            bankTransfer.getAmount(),
            bankTransfer.getStatus(),
            isNewlyCreated);
    }
}
