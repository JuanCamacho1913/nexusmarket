package com.nexusmarket.domain.service;

import com.nexusmarket.domain.models.InventoryItem;
import com.nexusmarket.domain.ports.out.InventoryItemRepositoryPort;
import com.nexusmarket.domain.exception.EntityNotFoundException;
import com.nexusmarket.domain.exception.InvariantViolationException;
import com.nexusmarket.domain.exception.ValidationException;
import com.nexusmarket.domain.service.support.IdGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepositoryPort inventoryItemRepository;
    private final IdGenerator idGenerator;

    public InventoryItem createItem(InventoryItem inventoryItem) {
        if (inventoryItem.getProduct() == null) {
            throw new ValidationException("InventoryItem.product must not be null");
        }
        if (inventoryItem.getWarehouse() == null) {
            throw new ValidationException("InventoryItem.warehouse must not be null");
        }
        inventoryItem.setId(idGenerator.newId());
        return inventoryItemRepository.save(inventoryItem);
    }

    public InventoryItem adjustStock(String itemId, int delta) {
        InventoryItem inventoryItem = inventoryItemRepository
                .findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem '" + itemId + "' does not exist"));
        int newQuantity = inventoryItem.getQuantity() + delta;
        if (newQuantity < 0) {
            throw new InvariantViolationException(
                    "InventoryItem '" + itemId + "' cannot be adjusted below zero quantity");
        }
        inventoryItem.setQuantity(newQuantity);
        return inventoryItemRepository.save(inventoryItem);
    }
}
