package com.nexusmarket.domain.exception;

public class ValidationException extends NexusMarketException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
