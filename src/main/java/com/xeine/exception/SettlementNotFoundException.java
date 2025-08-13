package com.xeine.exception;

public class SettlementNotFoundException extends RuntimeException {
    public SettlementNotFoundException(String message) {
        super(message);
    }
}
