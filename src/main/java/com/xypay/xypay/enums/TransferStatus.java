package com.xypay.xypay.enums;

/**
 * Transfer status enumeration
 */
public enum TransferStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    SUCCESS("success"),
    FAILED("failed"),
    CANCELLED("cancelled"),
    PENDING_APPROVAL("pending_approval"),
    APPROVED("approved"),
    REJECTED("rejected");
    
    private final String value;
    
    TransferStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}