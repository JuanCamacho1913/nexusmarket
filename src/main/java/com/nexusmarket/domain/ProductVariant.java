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
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "product")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false, unique = true)
    private String sku;

    private String variantName;

    @Builder.Default
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public ProductVariant(String sku, String variantName, BigDecimal priceAdjustment, Product product) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("El SKU no puede estar vacío");
        }
        if (product == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        this.sku = sku;
        this.variantName = variantName;
        this.priceAdjustment = priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO;
        this.product = product;
    }

    public BigDecimal getFinalPrice() {
        return product.getBasePrice().add(priceAdjustment);
    }
}
