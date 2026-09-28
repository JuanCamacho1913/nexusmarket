package com.nexusmarket.domain.service;

import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.ReturnRequest;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRequestRepositoryPort;
import com.nexusmarket.domain.exception.EntityNotFoundException;
import com.nexusmarket.domain.exception.InvariantViolationException;
import com.nexusmarket.domain.exception.ValidationException;
import com.nexusmarket.domain.service.support.IdGenerator;
import com.nexusmarket.domain.valueObjects.OrderStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReturnRequestService {

    private final OrderRepositoryPort orderRepository;
    private final ReturnRequestRepositoryPort returnRequestRepository;
    private final IdGenerator idGenerator;

    public ReturnRequest create(String orderId, String reason) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order '" + orderId + "' does not exist"));

        if (order.getStatus() != OrderStatus.DELIVERED_FINALIZED) {
            throw new InvariantViolationException(
                    "Order '" + orderId + "' must be DELIVERED_FINALIZED to request a return, but was "
                            + order.getStatus());
        }

        if (reason == null || reason.isBlank()) {
            throw new ValidationException("reason must not be blank");
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(idGenerator.newId());
        returnRequest.setOrder(order);
        returnRequest.setReason(reason);
        return returnRequestRepository.save(returnRequest);
    }
}
