package com.nexusmarket.domain.exception;

public class EntityNotFoundException extends NexusMarketException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
