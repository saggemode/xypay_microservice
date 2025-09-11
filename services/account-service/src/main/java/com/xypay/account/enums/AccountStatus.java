package com.xypay.account.enums;

public enum AccountStatus {
    ACTIVE("Active", "Account is active and operational"),
    INACTIVE("Inactive", "Account is inactive but not closed"),
    FROZEN("Frozen", "Account is temporarily frozen"),
    SUSPENDED("Suspended", "Account is suspended due to violations"),
    CLOSED("Closed", "Account is permanently closed"),
    DORMANT("Dormant", "Account is dormant due to inactivity"),
    RESTRICTED("Restricted", "Account has restrictions"),
    PENDING_APPROVAL("Pending Approval", "Account is pending approval"),
    REJECTED("Rejected", "Account application was rejected");

    private final String displayName;
    private final String description;

    AccountStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOperational() {
        return this == ACTIVE;
    }

    public boolean canTransact() {
        return this == ACTIVE;
    }

    public boolean isTerminal() {
        return this == CLOSED || this == REJECTED;
    }
}
