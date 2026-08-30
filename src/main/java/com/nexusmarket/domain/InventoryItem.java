package com.nexusmarket.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryItem {

    private String id;

    private int quantity;

    private Product product;

    private Warehouse warehouse;
}
