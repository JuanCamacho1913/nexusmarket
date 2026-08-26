package com.nexusmarket.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void constructorCalculaSubtotalComoCantidadPorPrecioUnitario() {
        Order order = Order.builder().id(1L).build();
        ProductVariant variant = ProductVariant.builder().id(1L).sku("SKU-1").build();

        OrderItem item = new OrderItem(order, variant, 3, new BigDecimal("19.90"));

        assertEquals(new BigDecimal("59.70"), item.getSubtotal());
    }

    @Test
    void cantidadCeroOMenorLanzaExcepcion() {
        Order order = Order.builder().id(1L).build();
        ProductVariant variant = ProductVariant.builder().id(1L).sku("SKU-1").build();

        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(order, variant, 0, new BigDecimal("19.90")));
    }
}
