package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root de la compra. Congela, a través de {@link OrderItem}, el precio
 * pagado por cada variante ya que el precio de un {@link ProductVariant} puede
 * cambiar luego de concretada la compra.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "items")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false, unique = true)
    private String orderTrackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "buyer_profile_id", nullable = false)
    private BuyerProfile buyerProfile;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public Order(String orderTrackingNumber, BuyerProfile buyerProfile) {
        if (orderTrackingNumber == null || orderTrackingNumber.isBlank()) {
            throw new IllegalArgumentException("El número de seguimiento no puede estar vacío");
        }
        if (buyerProfile == null) {
            throw new IllegalArgumentException("El comprador no puede ser nulo");
        }
        this.orderTrackingNumber = orderTrackingNumber;
        this.buyerProfile = buyerProfile;
        this.status = OrderStatus.CART;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("El ítem no puede ser nulo");
        }
        items.add(item);
        recalculateTotal();
    }

    /** Suma el subtotal de cada {@link OrderItem} y actualiza {@code totalAmount}. */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
