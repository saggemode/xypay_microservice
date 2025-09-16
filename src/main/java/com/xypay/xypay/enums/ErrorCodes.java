package com.xypay.xypay.enums;

/**
 * Error codes enumeration
 */
public enum ErrorCodes {
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS"),
    INVALID_ACCOUNT("INVALID_ACCOUNT"),
    INVALID_PIN("INVALID_PIN"),
    DUPLICATE_TRANSFER("DUPLICATE_TRANSFER"),
    LIMIT_EXCEEDED("LIMIT_EXCEEDED"),
    KYC_REQUIRED("KYC_REQUIRED"),
    WALLET_NOT_FOUND("WALLET_NOT_FOUND"),
    PROCESSING_ERROR("PROCESSING_ERROR"),
    LOCATION_REQUIRED("LOCATION_REQUIRED"),
    FRAUD_DETECTED("FRAUD_DETECTED"),
    TWO_FA_REQUIRED("TWO_FA_REQUIRED"),
    APPROVAL_REQUIRED("APPROVAL_REQUIRED");
    
    private final String value;
    
    ErrorCodes(String value) {
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
