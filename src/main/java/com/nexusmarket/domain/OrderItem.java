package com.nexusmarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Detalle congelado de una compra. Intencionalmente NO expone setters de
 * cantidad/precio: {@code unitPriceAtPurchase} es un snapshot tomado en el
 * momento de la compra y nunca se recalcula desde {@link ProductVariant}
 * (cuyo precio puede cambiar después). {@code subtotal} se calcula una única
 * vez en el constructor.
 */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "order")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false)
    private int quantity;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal unitPriceAtPurchase;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    public OrderItem(Order order, ProductVariant productVariant, int quantity, BigDecimal unitPriceAtPurchase) {
        if (order == null) {
            throw new IllegalArgumentException("La orden no puede ser nula");
        }
        if (productVariant == null) {
            throw new IllegalArgumentException("La variante de producto no puede ser nula");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (unitPriceAtPurchase == null || unitPriceAtPurchase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        this.order = order;
        this.productVariant = productVariant;
        this.quantity = quantity;
        this.unitPriceAtPurchase = unitPriceAtPurchase;
        this.subtotal = unitPriceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}
