package com.xypay.xypay.domain;

public enum NotificationLevel {
    LOW("low", "Low"),
    INFO("info", "Info"),
    SUCCESS("success", "Success"),
    WARNING("warning", "Warning"),
    ERROR("error", "Error"),
    CRITICAL("critical", "Critical");

    private final String code;
    private final String description;

    NotificationLevel(String code, String description) {
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