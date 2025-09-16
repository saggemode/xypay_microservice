package com.xypay.xypay.domain;

public enum TargetSavingFrequency {
    DAILY("daily", "Daily"),
    WEEKLY("weekly", "Weekly"),
    MONTHLY("monthly", "Monthly");
    
    private final String code;
    private final String description;
    
    TargetSavingFrequency(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
