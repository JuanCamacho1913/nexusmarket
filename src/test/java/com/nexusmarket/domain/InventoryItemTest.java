package com.nexusmarket.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryItemTest {

    @Test
    void decrementarMasDeLoDisponibleLanzaExcepcionYNoDejaNegativo() {
        InventoryItem inventoryItem = buildItem(5);

        assertThrows(IllegalStateException.class, () -> inventoryItem.decrementAvailable(10));
        assertEquals(5, inventoryItem.getAvailableQuantity());
    }

    @Test
    void reserveYReleaseMuevenCantidadesCorrectamente() {
        InventoryItem inventoryItem = buildItem(10);

        inventoryItem.reserve(4);
        assertEquals(6, inventoryItem.getAvailableQuantity());
        assertEquals(4, inventoryItem.getReservedQuantity());

        inventoryItem.release(4);
        assertEquals(10, inventoryItem.getAvailableQuantity());
        assertEquals(0, inventoryItem.getReservedQuantity());
    }

    @Test
    void reservarMasDeLoDisponibleLanzaExcepcion() {
        InventoryItem inventoryItem = buildItem(3);

        assertThrows(IllegalStateException.class, () -> inventoryItem.reserve(4));
    }

    private InventoryItem buildItem(int availableQuantity) {
        ProductVariant variant = ProductVariant.builder().id(1L).sku("SKU-1").build();
        Warehouse warehouse = Warehouse.builder().id(1L).build();
        return new InventoryItem(variant, warehouse, availableQuantity);
    }
}
