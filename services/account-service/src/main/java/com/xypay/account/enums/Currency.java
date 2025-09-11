package com.xypay.account.enums;

import java.math.BigDecimal;
import java.util.Currency;

public enum Currency {
    NGN("Nigerian Naira", "₦", 2),
    USD("US Dollar", "$", 2),
    EUR("Euro", "€", 2),
    GBP("British Pound", "£", 2),
    JPY("Japanese Yen", "¥", 0),
    CAD("Canadian Dollar", "C$", 2),
    AUD("Australian Dollar", "A$", 2),
    CHF("Swiss Franc", "CHF", 2),
    CNY("Chinese Yuan", "¥", 2),
    INR("Indian Rupee", "₹", 2),
    ZAR("South African Rand", "R", 2),
    GHS("Ghanaian Cedi", "₵", 2),
    KES("Kenyan Shilling", "KSh", 2),
    EGP("Egyptian Pound", "E£", 2),
    MAD("Moroccan Dirham", "MAD", 2);

    private final String fullName;
    private final String symbol;
    private final int decimalPlaces;

    Currency(String fullName, String symbol, int decimalPlaces) {
        this.fullName = fullName;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public String getCode() {
        return this.name();
    }

    public BigDecimal getMinimumAmount() {
        return BigDecimal.valueOf(Math.pow(10, -decimalPlaces));
    }

    public BigDecimal getMaximumAmount() {
        return BigDecimal.valueOf(999999999.99);
    }

    public boolean isValidAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        BigDecimal minAmount = getMinimumAmount();
        BigDecimal maxAmount = getMaximumAmount();
        
        return amount.compareTo(minAmount) >= 0 && amount.compareTo(maxAmount) <= 0;
    }

    public static Currency fromCode(String code) {
        try {
            return Currency.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NGN; // Default to NGN
        }
    }
}
