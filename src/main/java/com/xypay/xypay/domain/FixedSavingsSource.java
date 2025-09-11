package com.xypay.xypay.domain;

public enum FixedSavingsSource {
    WALLET("wallet", "Wallet"),
    XYSAVE("xysave", "XySave"),
    BOTH("both", "Both Wallet and XySave");

    private final String code;
    private final String description;

    FixedSavingsSource(String code, String description) {
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