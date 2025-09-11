package com.xypay.notification.domain;

public enum NotificationLevel {
    INFO("info", "Information"),
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
