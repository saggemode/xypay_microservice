package com.xypay.xypay.domain;

public enum FixedSavingsPurpose {
    EDUCATION("education", "Education"),
    BUSINESS("business", "Business"),
    INVESTMENT("investment", "Investment"),
    EMERGENCY("emergency", "Emergency Fund"),
    TRAVEL("travel", "Travel"),
    WEDDING("wedding", "Wedding"),
    VEHICLE("vehicle", "Vehicle"),
    HOME_RENOVATION("home_renovation", "Home Renovation"),
    MEDICAL("medical", "Medical"),
    RETIREMENT("retirement", "Retirement"),
    OTHER("other", "Other");

    private final String code;
    private final String description;

    FixedSavingsPurpose(String code, String description) {
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