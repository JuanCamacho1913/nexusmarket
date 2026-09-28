package com.nexusmarket.domain.service;

import com.nexusmarket.domain.models.BillingInvoice;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.out.BillingInvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.exception.DuplicateEntityException;
import com.nexusmarket.domain.exception.EntityNotFoundException;
import com.nexusmarket.domain.exception.InvariantViolationException;
import com.nexusmarket.domain.service.support.IdGenerator;
import com.nexusmarket.domain.valueObjects.OrderStatus;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillingInvoiceService {

    private static final Set<OrderStatus> ELIGIBLE_STATUSES =
            EnumSet.of(OrderStatus.PAID, OrderStatus.DISPATCHED, OrderStatus.DELIVERED_FINALIZED);

    private final OrderRepositoryPort orderRepository;
    private final BillingInvoiceRepositoryPort billingInvoiceRepository;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public BillingInvoice issueFor(String orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order '" + orderId + "' does not exist"));

        if (!ELIGIBLE_STATUSES.contains(order.getStatus())) {
            throw new InvariantViolationException(
                    "Order '" + orderId + "' must be PAID, DISPATCHED, or DELIVERED_FINALIZED to be invoiced, but was "
                            + order.getStatus());
        }

        if (billingInvoiceRepository.findByOrderId(orderId).isPresent()) {
            throw new DuplicateEntityException("Order '" + orderId + "' already has a BillingInvoice");
        }

        BillingInvoice invoice = new BillingInvoice();
        invoice.setId(idGenerator.newId());
        invoice.setOrder(order);
        invoice.setAmount(order.getTotalAmount());
        invoice.setIssuedAt(LocalDateTime.now(clock));
        return billingInvoiceRepository.save(invoice);
    }

    public Optional<BillingInvoice> findByOrder(String orderId) {
        return billingInvoiceRepository.findByOrderId(orderId);
    }
}
