package com.xypay.xypay.enums;

/**
 * Security level enumeration
 */
public enum SecurityLevel {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    CRITICAL("critical");
    
    private final String value;
    
    SecurityLevel(String value) {
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