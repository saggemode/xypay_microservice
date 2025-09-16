package com.xypay.xypay.domain;

public enum TargetSavingSource {
    WALLET("wallet", "Wallet"),
    XYSAVE("xysave", "XySave Account"),
    BOTH("both", "Both (50/50)");
    
    private final String code;
    private final String description;
    
    TargetSavingSource(String code, String description) {
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
