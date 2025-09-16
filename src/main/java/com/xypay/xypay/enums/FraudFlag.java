package com.xypay.xypay.enums;

/**
 * Fraud detection flag levels
 * Equivalent to Django's FraudFlag
 */
public enum FraudFlag {
    NORMAL("normal", "Normal"),
    LOW("low", "Low Risk"),
    MEDIUM("medium", "Medium Risk"),
    HIGH("high", "High Risk"),
    CRITICAL("critical", "Critical Risk");
    
    private final String code;
    private final String displayName;
    
    FraudFlag(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return code;
    }
}
