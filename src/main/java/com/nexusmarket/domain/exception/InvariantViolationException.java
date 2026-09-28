package com.nexusmarket.domain.exception;

public class InvariantViolationException extends NexusMarketException {

    public InvariantViolationException(String message) {
        super(message);
    }

    public InvariantViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
