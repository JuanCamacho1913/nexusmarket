package com.nexusmarket.domain.service;

import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.OrderItem;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.exception.EntityNotFoundException;
import com.nexusmarket.domain.exception.InvariantViolationException;
import com.nexusmarket.domain.exception.ValidationException;
import com.nexusmarket.domain.service.support.IdGenerator;
import com.nexusmarket.domain.valueObjects.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Map<OrderStatus, OrderStatus> ALLOWED_NEXT_STATUS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_NEXT_STATUS.put(OrderStatus.CART, OrderStatus.PENDING_PAYMENT);
        ALLOWED_NEXT_STATUS.put(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);
        ALLOWED_NEXT_STATUS.put(OrderStatus.PAID, OrderStatus.DISPATCHED);
        ALLOWED_NEXT_STATUS.put(OrderStatus.DISPATCHED, OrderStatus.DELIVERED_FINALIZED);
    }

    private final OrderRepositoryPort orderRepository;
    private final ProductRepositoryPort productRepository;
    private final IdGenerator idGenerator;

    public Order createOrder(Order order) {
        if (order.getBuyerProfile() == null) {
            throw new ValidationException("Order.buyerProfile must not be null");
        }
        order.setId(idGenerator.newId());
        order.setStatus(OrderStatus.CART);
        order.setTotalAmount(BigDecimal.ZERO);
        replaceItems(order, new ArrayList<>());
        return orderRepository.save(order);
    }

    public Order addItem(String orderId, String productId, int quantity) {
        Order order = requireOrder(orderId);
        assertMutable(order);
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product '" + productId + "' does not exist"));

        OrderItem item = new OrderItem();
        item.setId(idGenerator.newId());
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setOrder(order);

        List<OrderItem> items = new ArrayList<>(order.getItems());
        items.add(item);
        replaceItems(order, items);

        recalculateTotal(order);
        return orderRepository.save(order);
    }

    public Order removeItem(String orderId, String orderItemId) {
        Order order = requireOrder(orderId);
        assertMutable(order);

        List<OrderItem> items = new ArrayList<>(order.getItems());
        items.removeIf(item -> orderItemId.equals(item.getId()));
        replaceItems(order, items);

        recalculateTotal(order);
        return orderRepository.save(order);
    }

    public Order changeQuantity(String orderId, String orderItemId, int newQuantity) {
        Order order = requireOrder(orderId);
        assertMutable(order);

        OrderItem item = order.getItems().stream()
                .filter(candidate -> orderItemId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("OrderItem '" + orderItemId + "' does not exist"));
        item.setQuantity(newQuantity);

        recalculateTotal(order);
        return orderRepository.save(order);
    }

    public Order advanceStatus(String orderId, OrderStatus newStatus) {
        Order order = requireOrder(orderId);
        assertMutable(order);

        OrderStatus allowedNext = ALLOWED_NEXT_STATUS.get(order.getStatus());
        if (allowedNext == null || allowedNext != newStatus) {
            throw new InvariantViolationException(
                    "Order '" + orderId + "' cannot transition from " + order.getStatus() + " to " + newStatus);
        }
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    private void assertMutable(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED_FINALIZED) {
            throw new InvariantViolationException(
                    "Order '" + order.getId() + "' is DELIVERED_FINALIZED and cannot be mutated");
        }
    }

    private void recalculateTotal(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmount(total);
    }

    private void replaceItems(Order order, List<OrderItem> items) {
        order.setItems(Collections.unmodifiableList(items));
    }

    private Order requireOrder(String orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order '" + orderId + "' does not exist"));
    }
}
