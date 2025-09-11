package com.xypay.xypay.service;

/**
 * Standardized error codes for transaction processing.
 */
public class StandardizedErrorCodes {
    /** Standardized error codes for transaction processing. */
    public static final String INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
    public static final String SELF_TRANSFER_ATTEMPT = "SELF_TRANSFER_ATTEMPT";
    public static final String WALLET_NOT_FOUND = "WALLET_NOT_FOUND";
    public static final String PROCESSING_ERROR = "PROCESSING_ERROR";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
    public static final String FRAUD_DETECTION = "FRAUD_DETECTION";
    public static final String LIMIT_EXCEEDED = "LIMIT_EXCEEDED";
    public static final String KYC_REQUIRED = "KYC_REQUIRED";
    public static final String ACCOUNT_BLOCKED = "ACCOUNT_BLOCKED";
    public static final String INVALID_ACCOUNT = "INVALID_ACCOUNT";
    public static final String BANK_SERVICE_UNAVAILABLE = "BANK_SERVICE_UNAVAILABLE";
    public static final String TIMEOUT_ERROR = "TIMEOUT_ERROR";
    public static final String DUPLICATE_TRANSACTION = "DUPLICATE_TRANSACTION";
    
    private StandardizedErrorCodes() {
        // Private constructor to prevent instantiation
    }
}