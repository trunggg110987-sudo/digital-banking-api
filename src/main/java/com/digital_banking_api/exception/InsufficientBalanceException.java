package com.digital_banking_api.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
