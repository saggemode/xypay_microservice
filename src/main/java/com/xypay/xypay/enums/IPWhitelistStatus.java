package com.xypay.xypay.enums;

/**
 * IP Whitelist status choices
 * Equivalent to Django's IPWhitelistStatus
 */
public enum IPWhitelistStatus {
    PENDING("pending", "Pending"),
    APPROVED("approved", "Approved"),
    REJECTED("rejected", "Rejected");
    
    private final String code;
    private final String displayName;
    
    IPWhitelistStatus(String code, String displayName) {
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
