package com.nexusmarket.domain.exceptions;


/**
 * RG-03: ningun participante administra informacion fuera de su rol.
 */
public class ForbiddenOperationException extends DomainException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
