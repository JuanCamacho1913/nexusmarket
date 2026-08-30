package com.nexusmarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Registro de stock de un producto en un almacén determinado. Invariante
 * estricta: {@code quantity} nunca puede quedar negativo. La única vía de
 * mutación es {@link #adjust(int)}; el campo no tiene setter.
 */
@Entity
@Table(name = "inventory_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = {"product", "warehouse"})
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private int quantity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    /**
     * Única vía de mutación de {@code quantity}. Suma {@code delta} (positivo para
     * reponer, negativo para consumir). Rechaza cualquier ajuste que dejaría el
     * inventario en negativo, sin modificar el estado.
     */
    public void adjust(int delta) {
        int result = this.quantity + delta;
        if (result < 0) {
            throw new IllegalArgumentException("El inventario no puede quedar en negativo");
        }
        this.quantity = result;
    }
}
