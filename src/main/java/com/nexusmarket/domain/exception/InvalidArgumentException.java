package com.nexusmarket.domain.exception;

/**
 * Se lanza cuando un dato recibido por un constructor o un metodo del
 * dominio no cumple una validacion basica (null, vacio, fuera de rango,
 * formato invalido).
 */
public class InvalidArgumentException extends DomainException {

    public InvalidArgumentException(String message) {
        super(message);
    }
}
