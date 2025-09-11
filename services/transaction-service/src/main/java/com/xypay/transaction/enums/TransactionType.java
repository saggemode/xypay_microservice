package com.xypay.transaction.enums;

public enum TransactionType {
    DEPOSIT("DEPOSIT", "Money deposited into account"),
    WITHDRAWAL("WITHDRAWAL", "Money withdrawn from account"),
    TRANSFER("TRANSFER", "Transfer between accounts"),
    BILL_PAYMENT("BILL_PAYMENT", "Payment for bills/services"),
    REFUND("REFUND", "Refund transaction"),
    FEE("FEE", "Service fee charged"),
    INTEREST("INTEREST", "Interest credited"),
    REVERSAL("REVERSAL", "Transaction reversal"),
    ADJUSTMENT("ADJUSTMENT", "Manual adjustment"),
    CASHBACK("CASHBACK", "Cashback reward");
    
    private final String code;
    private final String description;
    
    TransactionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TransactionType fromCode(String code) {
        for (TransactionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type code: " + code);
    }
    
    public boolean isDebit() {
        return this == WITHDRAWAL || this == TRANSFER || this == BILL_PAYMENT || 
               this == FEE || this == REVERSAL;
    }
    
    public boolean isCredit() {
        return this == DEPOSIT || this == REFUND || this == INTEREST || 
               this == ADJUSTMENT || this == CASHBACK;
    }
}
