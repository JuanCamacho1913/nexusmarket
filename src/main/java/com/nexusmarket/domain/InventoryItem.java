package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.InventoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Registro de stock de una variante de producto en un almacén determinado.
 * Invariante estricta: availableQuantity nunca puede quedar negativo; toda
 * mutación pasa por los métodos de dominio (reserve/release/decrementAvailable)
 * o por el setter validado, nunca se asigna el campo directamente.
 */
@Entity
@Table(name = "inventory_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_variant_id", "warehouse_id"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = {"productVariant", "warehouse"})
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false)
    private int availableQuantity;

    @Column(nullable = false)
    private int reservedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    public InventoryItem(ProductVariant productVariant, Warehouse warehouse, int availableQuantity) {
        if (productVariant == null) {
            throw new IllegalArgumentException("La variante de producto no puede ser nula");
        }
        if (warehouse == null) {
            throw new IllegalArgumentException("El almacén no puede ser nulo");
        }
        if (availableQuantity < 0) {
            throw new IllegalStateException("La cantidad disponible no puede ser negativa");
        }
        this.productVariant = productVariant;
        this.warehouse = warehouse;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
        refreshStatus();
    }

    /** Setter validado: nunca deja el campo en negativo. */
    public void setAvailableQuantity(int availableQuantity) {
        if (availableQuantity < 0) {
            throw new IllegalStateException("La cantidad disponible no puede ser negativa");
        }
        this.availableQuantity = availableQuantity;
    }

    /** Mueve cantidad de disponible a reservado. */
    public void reserve(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser mayor a cero");
        }
        if (availableQuantity < qty) {
            throw new IllegalStateException("No hay suficiente stock disponible para reservar");
        }
        availableQuantity -= qty;
        reservedQuantity += qty;
        refreshStatus();
    }

    /** Revierte una reserva previa, devolviendo cantidad a disponible. */
    public void release(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("La cantidad a liberar debe ser mayor a cero");
        }
        if (reservedQuantity < qty) {
            throw new IllegalStateException("No hay suficiente cantidad reservada para liberar");
        }
        reservedQuantity -= qty;
        availableQuantity += qty;
        refreshStatus();
    }

    /** Descuenta stock disponible definitivamente (p. ej. al despachar). */
    public void decrementAvailable(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero");
        }
        if (availableQuantity - qty < 0) {
            throw new IllegalStateException("No se puede dejar la cantidad disponible en negativo");
        }
        availableQuantity -= qty;
        refreshStatus();
    }

    private void refreshStatus() {
        if (availableQuantity == 0 && reservedQuantity == 0) {
            this.status = InventoryStatus.OUT_OF_STOCK;
        } else if (availableQuantity == 0) {
            this.status = InventoryStatus.RESERVED;
        } else {
            this.status = InventoryStatus.AVAILABLE;
        }
    }
}
