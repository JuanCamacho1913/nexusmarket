package com.nexusmarket.valueObjects;

/**
 * Ciclo de vida de una {@code ReturnRequest}. No especificado en el enunciado original;
 * se agrega para modelar el flujo de aprobación de devoluciones.
 */
public enum ReturnStatus {
    REQUESTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    COMPLETED
}
