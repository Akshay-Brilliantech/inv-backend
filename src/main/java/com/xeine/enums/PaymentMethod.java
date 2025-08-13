package com.xeine.enums;


public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    UPI("UPI"),
    CHEQUE("Cheque"),
    ONLINE_PAYMENT("Online Payment"),
    WALLET("Digital Wallet");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Default payment method
    public static PaymentMethod getDefault() {
        return CASH;
    }
}