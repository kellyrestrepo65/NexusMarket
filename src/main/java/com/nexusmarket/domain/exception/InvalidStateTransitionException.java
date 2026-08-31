package com.nexusmarket.domain.exception;

/**
 * Se lanza cuando se intenta cambiar el estado de una orden, envio,
 * producto, devolucion o reembolso a un estado que no esta permitido
 * desde su estado actual.
 */
public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
