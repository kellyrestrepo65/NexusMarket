package com.nexusmarket.domain.exceptions;


/**
 * Seccion 11: el documento de identidad y el correo deben ser unicos.
 */
public class DuplicateDataException extends DomainException {

    public DuplicateDataException(String message) {
        super(message);
    }
}
