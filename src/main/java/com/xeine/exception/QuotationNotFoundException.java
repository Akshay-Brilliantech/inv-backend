package com.xeine.exception;

public class QuotationNotFoundException extends RuntimeException {
    public QuotationNotFoundException(String message) {
        super(message);
    }
}
