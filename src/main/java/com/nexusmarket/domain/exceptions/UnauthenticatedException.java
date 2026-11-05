package com.nexusmarket.domain.exceptions;


/**
 * RG-01: toda operacion la ejecuta un usuario autenticado. Se lanza cuando
 * no llega el usuario o no existe en el sistema.
 */
public class UnauthenticatedException extends DomainException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
