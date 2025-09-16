package com.xypay.xypay.enums;

/**
 * Device fingerprint type choices
 * Equivalent to Django's DeviceFingerprint.CHOICES
 */
public enum DeviceFingerprintType {
    MOBILE("mobile", "Mobile"),
    TABLET("tablet", "Tablet"),
    DESKTOP("desktop", "Desktop"),
    UNKNOWN("unknown", "Unknown");
    
    private final String code;
    private final String displayName;
    
    DeviceFingerprintType(String code, String displayName) {
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
