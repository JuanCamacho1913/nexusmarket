package com.nexusmarket.domain.exception;

public class UnauthorizedOperationException extends NexusMarketException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }

    public UnauthorizedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
