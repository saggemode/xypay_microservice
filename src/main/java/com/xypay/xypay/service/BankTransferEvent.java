package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;

/**
 * Event representing a bank transfer.
 */
public class BankTransferEvent {
    
    private BankTransfer transfer;
    private boolean created;
    
    public BankTransferEvent(BankTransfer transfer, boolean created) {
        this.transfer = transfer;
        this.created = created;
    }
    
    // Getters and setters
    public BankTransfer getTransfer() {
        return transfer;
    }
    
    public void setTransfer(BankTransfer transfer) {
        this.transfer = transfer;
    }
    
    public boolean isCreated() {
        return created;
    }
    
    public void setCreated(boolean created) {
        this.created = created;
    }
}