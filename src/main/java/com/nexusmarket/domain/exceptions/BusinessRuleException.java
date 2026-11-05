package com.nexusmarket.domain.exceptions;

/**
 * Se lanza cuando una operacion esta bien formada pero rompe una regla de
 * negocio, por ejemplo un comprador restringido que intenta usar el carrito.
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
