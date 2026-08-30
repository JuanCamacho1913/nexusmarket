package com.nexusmarket.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
public class OrderItem {

    private String id;

    private int quantity;

    private BigDecimal unitPrice;

    private Order order;

    private Product product;
}
