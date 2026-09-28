package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.BillingInvoice;

import java.util.Optional;

public interface BillingInvoiceRepositoryPort {

    BillingInvoice save(BillingInvoice billingInvoice);

    Optional<BillingInvoice> findByOrderId(String orderId);
}
