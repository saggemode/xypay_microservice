package com.xypay.xypay.domain;

public enum TargetSavingCategory {
    ACCOMMODATION("accommodation", "Accommodation"),
    EDUCATION("education", "Education"),
    BUSINESS("business", "Business"),
    JAPA("japa", "Japa (Relocation)"),
    VEHICLE("vehicle", "Vehicle"),
    WEDDING("wedding", "Wedding"),
    EMERGENCY("emergency", "Emergency Fund"),
    INVESTMENT("investment", "Investment"),
    TRAVEL("travel", "Travel"),
    HOME_RENOVATION("home_renovation", "Home Renovation"),
    MEDICAL("medical", "Medical"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    OTHER("other", "Other");
    
    private final String code;
    private final String description;
    
    TargetSavingCategory(String code, String description) {
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
