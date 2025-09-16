package com.xypay.xypay.enums;

/**
 * KYC Level choices
 * Equivalent to Django's KYCLevelChoices
 */
public enum KYCLevelChoices {
    TIER_1("tier_1", "Tier 1"),
    TIER_2("tier_2", "Tier 2"),
    TIER_3("tier_3", "Tier 3");
    
    private final String code;
    private final String displayName;
    
    KYCLevelChoices(String code, String displayName) {
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
