package com.xypay.xypay.enums;

/**
 * Transfer type enumeration
 */
public enum TransferType {
    INTERNAL("internal"),
    EXTERNAL("external"),
    BILL_PAYMENT("bill_payment"),
    AIRTIME("airtime"),
    DATA("data");
    
    private final String value;
    
    TransferType(String value) {
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