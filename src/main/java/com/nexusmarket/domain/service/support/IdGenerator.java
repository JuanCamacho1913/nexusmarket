package com.nexusmarket.domain.service.support;

/**
 * Port for generating new entity identifiers. Injected into services so an
 * identifier can be assigned before {@code save(entity)} (for example, to
 * link a newly created BillingInvoice to its Order in the same call).
 */
public interface IdGenerator {

    String newId();
}
