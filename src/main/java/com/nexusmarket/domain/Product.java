package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.ProductStatus;
import com.nexusmarket.valueObjects.ProductType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Product {

    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private ProductType type;

    private ProductStatus status = ProductStatus.PUBLISHED;

    private SellerProfile sellerProfile;

    private List<String> variants = new ArrayList<>();
}
