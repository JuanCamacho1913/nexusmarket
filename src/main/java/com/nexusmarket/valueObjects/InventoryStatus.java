package com.nexusmarket.valueObjects;

/**
 * Estado del stock de un {@code InventoryItem}. No especificado en el enunciado original;
 * se agrega para soportar las invariantes de reserva/liberación de inventario.
 */
public enum InventoryStatus {
    AVAILABLE,
    RESERVED,
    OUT_OF_STOCK
}
