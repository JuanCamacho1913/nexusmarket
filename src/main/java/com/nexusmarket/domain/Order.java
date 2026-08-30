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
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root de la compra. Congela, a través de {@link OrderItem}, el precio
 * pagado por cada producto. Una orden en estado {@code DELIVERED_FINALIZED} es
 * inmutable: todo mutador es rechazado.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "items")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.CART;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_profile_id", nullable = false)
    private BuyerProfile buyerProfile;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(OrderItem item) {
        assertNotFinalized();
        if (item == null) {
            throw new IllegalArgumentException("El ítem no puede ser nulo");
        }
        items.add(item);
    }

    public void updateStatus(OrderStatus newStatus) {
        assertNotFinalized();
        this.status = newStatus;
    }

    public void setTotalAmount(BigDecimal amount) {
        assertNotFinalized();
        this.totalAmount = amount;
    }

    public void setBuyerProfile(BuyerProfile bp) {
        assertNotFinalized();
        this.buyerProfile = bp;
    }

    private void assertNotFinalized() {
        if (this.status == OrderStatus.DELIVERED_FINALIZED) {
            throw new IllegalStateException("Un pedido finalizado no puede modificarse");
        }
    }
}
