package com.nexusmarket.domain.exception;

/**
 * Se lanza cuando se intenta modificar una orden que ya esta en estado
 * DELIVERED. Es una excepcion aparte de InvalidStateTransitionException
 * porque aqui el problema no es el cambio de estado, sino que se esta
 * intentando tocar los items de una orden que ya no se puede modificar.
 */
public class OrderFinalizedException extends DomainException {

    public OrderFinalizedException(String message) {
        super(message);
    }
}
