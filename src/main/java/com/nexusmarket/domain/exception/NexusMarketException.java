package com.nexusmarket.domain.exception;

public abstract class NexusMarketException extends RuntimeException {

    protected NexusMarketException(String message) {
        super(message);
    }

    protected NexusMarketException(String message, Throwable cause) {
        super(message, cause);
    }
}
