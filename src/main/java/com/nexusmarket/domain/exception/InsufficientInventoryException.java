package com.nexusmarket.domain.exception;

/**
 * Se lanza cuando un movimiento de inventario (reservar, liberar,
 * despachar o ajustar) dejaria el stock negativo o reservaria mas de
 * lo que hay disponible.
 */
public class InsufficientInventoryException extends DomainException {

    public InsufficientInventoryException(String message) {
        super(message);
    }
}
