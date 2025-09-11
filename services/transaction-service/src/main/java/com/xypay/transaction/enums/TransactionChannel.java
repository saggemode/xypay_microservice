package com.xypay.transaction.enums;

public enum TransactionChannel {
    MOBILE_APP("MOBILE_APP", "Mobile application"),
    WEB_APP("WEB_APP", "Web application"),
    ATM("ATM", "Automated Teller Machine"),
    POS("POS", "Point of Sale terminal"),
    BANK_TRANSFER("BANK_TRANSFER", "Bank transfer"),
    CARD("CARD", "Card payment"),
    USSD("USSD", "USSD banking"),
    API("API", "API integration"),
    SYSTEM("SYSTEM", "System generated"),
    ADMIN("ADMIN", "Admin panel"),
    BRANCH("BRANCH", "Bank branch"),
    AGENT("AGENT", "Agent network");
    
    private final String code;
    private final String description;
    
    TransactionChannel(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TransactionChannel fromCode(String code) {
        for (TransactionChannel channel : values()) {
            if (channel.code.equals(code)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Invalid transaction channel code: " + code);
    }
    
    public boolean isDigital() {
        return this == MOBILE_APP || this == WEB_APP || this == API || this == USSD;
    }
    
    public boolean isPhysical() {
        return this == ATM || this == POS || this == BRANCH || this == AGENT;
    }
    
    public boolean isSystem() {
        return this == SYSTEM || this == ADMIN;
    }
}
