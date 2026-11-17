package com.nexusmarket.domain.exceptions;

/**
 * Se lanza cuando un caso de uso busca una entidad por su id y no existe.
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entity, Object id) {
        super(entity + " with id " + id + " not found");
    }
}
