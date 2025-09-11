package com.xypay.xypay.enums;

/**
 * Standardized error codes for better failure tracking in bank transfers.
 * Equivalent to Django's TransferErrorCodes class.
 */
public enum TransferErrorCodes {
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS"),
    SELF_TRANSFER_ATTEMPT("SELF_TRANSFER_ATTEMPT"),
    WALLET_NOT_FOUND("WALLET_NOT_FOUND"),
    PROCESSING_ERROR("PROCESSING_ERROR"),
    DATABASE_ERROR("DATABASE_ERROR"),
    VALIDATION_ERROR("VALIDATION_ERROR"),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR"),
    FRAUD_DETECTION("FRAUD_DETECTION"),
    LIMIT_EXCEEDED("LIMIT_EXCEEDED"),
    KYC_REQUIRED("KYC_REQUIRED"),
    ACCOUNT_BLOCKED("ACCOUNT_BLOCKED"),
    INVALID_ACCOUNT("INVALID_ACCOUNT"),
    BANK_SERVICE_UNAVAILABLE("BANK_SERVICE_UNAVAILABLE"),
    TIMEOUT_ERROR("TIMEOUT_ERROR"),
    DUPLICATE_TRANSACTION("DUPLICATE_TRANSACTION");
    
    private final String code;
    
    TransferErrorCodes(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    @Override
    public String toString() {
        return code;
    }
}
