package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class Order {

    private String id;

    private OrderStatus status = OrderStatus.CART;

    private BigDecimal totalAmount = BigDecimal.ZERO;

    private BuyerProfile buyerProfile;

    private List<OrderItem> items = new ArrayList<>();
}
