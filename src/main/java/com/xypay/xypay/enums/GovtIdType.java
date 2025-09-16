package com.xypay.xypay.enums;

/**
 * Government ID type choices
 * Equivalent to Django's GOVT_ID_TYPE_CHOICES
 */
public enum GovtIdType {
    NATIONAL_ID("national_id", "National ID Card"),
    VOTERS_CARD("voters_card", "Voter's Card"),
    PASSPORT("passport", "International Passport"),
    DRIVERS_LICENSE("drivers_license", "Driver's License");
    
    private final String code;
    private final String displayName;
    
    GovtIdType(String code, String displayName) {
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
