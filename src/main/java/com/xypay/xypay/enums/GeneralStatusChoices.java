package com.xypay.xypay.enums;

/**
 * General status choices used across the application
 * Equivalent to Django's GeneralStatusChoices
 */
public enum GeneralStatusChoices {
    // Transaction types
    CREDIT("CREDIT"),
    DEBIT("DEBIT"),
    
    // Transaction channels
    TRANSFER("TRANSFER"),
    DEPOSIT("DEPOSIT"),
    BILL("BILL"),
    
    // Transaction statuses
    SUCCESS("SUCCESS"),
    PENDING("PENDING"),
    FAILED("FAILED"),
    PROCESSING("PROCESSING"),
    SUCCESSFUL("SUCCESSFUL"),
    COMPLETED("COMPLETED"),
    
    // General statuses
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    BLOCKED("BLOCKED");
    
    private final String value;
    
    GeneralStatusChoices(String value) {
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
