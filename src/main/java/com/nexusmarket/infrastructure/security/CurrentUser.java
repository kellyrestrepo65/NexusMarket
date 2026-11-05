package com.nexusmarket.infrastructure.security;

import com.nexusmarket.domain.models.user.User;

import com.nexusmarket.domain.exceptions.UnauthenticatedException;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * La seccion 3.2 deja fuera del alcance los mecanismos tecnicos de
 * autenticacion, asi que quien llama se identifica con la cabecera X-User-Id.
 * Luego el dominio verifica que ese usuario exista, este activo y tenga el rol
 * requerido.
 */
public final class CurrentUser {

    public static final String HEADER = "X-User-Id";

    private CurrentUser() {
    }

    public static EntityId of(String header) {
        if (header == null || header.isBlank()) {
            throw new UnauthenticatedException("The " + HEADER + " header is required");
        }
        return EntityId.of(header.trim());
    }
}
