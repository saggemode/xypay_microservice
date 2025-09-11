package com.xypay.xypay.service;


/**
 * Event representing a failed bank transfer.
 */
public class BankTransferFailedEvent {
    
    private Long transferId;
    private String errorCode;
    private String errorMessage;
    
    public BankTransferFailedEvent(Long transferId, String errorCode, String errorMessage) {
        this.transferId = transferId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    // Getters and setters
    public Long getTransferId() {
        return transferId;
    }
    
    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}