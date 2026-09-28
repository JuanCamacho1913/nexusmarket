package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.InventoryItem;

import java.util.Optional;

public interface InventoryItemRepositoryPort {

    InventoryItem save(InventoryItem inventoryItem);

    Optional<InventoryItem> findById(String id);
}
