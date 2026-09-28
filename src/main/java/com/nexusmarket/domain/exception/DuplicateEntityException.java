package com.nexusmarket.domain.exception;

public class DuplicateEntityException extends NexusMarketException {

    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
