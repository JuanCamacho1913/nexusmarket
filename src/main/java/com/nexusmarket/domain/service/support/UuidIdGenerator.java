package com.nexusmarket.domain.service.support;

import java.util.UUID;

/**
 * Default {@link IdGenerator} implementation backed by random UUIDs.
 */
public class UuidIdGenerator implements IdGenerator {

    @Override
    public String newId() {
        return UUID.randomUUID().toString();
    }
}
