package com.xypay.xypay.domain;

public enum FixedSavingsPurpose {
    EMERGENCY_FUND("emergency_fund", "Emergency Fund"),
    VACATION("vacation", "Vacation"),
    EDUCATION("education", "Education"),
    HOME_IMPROVEMENT("home_improvement", "Home Improvement"),
    WEDDING("wedding", "Wedding"),
    BUSINESS("business", "Business"),
    RETIREMENT("retirement", "Retirement"),
    HEALTH("health", "Health"),
    INVESTMENT("investment", "Investment"),
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