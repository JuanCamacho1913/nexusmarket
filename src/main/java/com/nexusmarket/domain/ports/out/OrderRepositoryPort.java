package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Order;

import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(String id);
}
