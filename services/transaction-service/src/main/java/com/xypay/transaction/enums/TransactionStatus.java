package com.xypay.transaction.enums;

public enum TransactionStatus {
    PENDING("PENDING", "Transaction is pending processing"),
    PROCESSING("PROCESSING", "Transaction is being processed"),
    SUCCESS("SUCCESS", "Transaction completed successfully"),
    FAILED("FAILED", "Transaction failed"),
    CANCELLED("CANCELLED", "Transaction was cancelled"),
    REVERSED("REVERSED", "Transaction was reversed"),
    EXPIRED("EXPIRED", "Transaction expired"),
    ON_HOLD("ON_HOLD", "Transaction is on hold");
    
    private final String code;
    private final String description;
    
    TransactionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TransactionStatus fromCode(String code) {
        for (TransactionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid transaction status code: " + code);
    }
    
    public boolean isFinal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || 
               this == REVERSED || this == EXPIRED;
    }
    
    public boolean isActive() {
        return this == PENDING || this == PROCESSING || this == ON_HOLD;
    }
    
    public boolean canBeReversed() {
        return this == SUCCESS;
    }
}
