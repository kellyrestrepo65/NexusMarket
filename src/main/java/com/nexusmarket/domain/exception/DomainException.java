package com.nexusmarket.domain.exception;

/**
 * Clase base de todas las excepciones del dominio de NexusMarket.
 * Es unchecked (extiende RuntimeException) para no obligar a poner
 * try/catch en todos lados donde se valida una regla de negocio.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
