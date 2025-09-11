package com.xypay.notification.domain;

public enum NotificationStatus {
    PENDING("pending", "Pending"),
    SENT("sent", "Sent"),
    DELIVERED("delivered", "Delivered"),
    READ("read", "Read"),
    FAILED("failed", "Failed");

    private final String code;
    private final String description;

    NotificationStatus(String code, String description) {
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
