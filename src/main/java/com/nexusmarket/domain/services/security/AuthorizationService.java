package com.nexusmarket.domain.services.security;

import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.exceptions.UnauthenticatedException;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Aplica RG-01 (toda operacion la ejecuta un usuario identificado y activo) y
 * RG-03 (cada rol solo trabaja sobre los procesos de la matriz de
 * responsabilidades).
 */
@Service
public class AuthorizationService {

    private final UserRepositoryPort userRepositoryPort;

    public AuthorizationService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public User requireActiveUser(EntityId actorId) {
        if (actorId == null) {
            throw new UnauthenticatedException("The operation requires an identified user");
        }
        User actor = userRepositoryPort.findById(actorId)
                .orElseThrow(() -> new UnauthenticatedException("The user " + actorId + " is not registered"));
        if (!actor.isActive()) {
            throw new ForbiddenOperationException("The user " + actorId + " is not active");
        }
        return actor;
    }

    public User requireRole(EntityId actorId, Role... allowedRoles) {
        User actor = requireActiveUser(actorId);
        if (!Arrays.asList(allowedRoles).contains(actor.getRole())) {
            throw new ForbiddenOperationException(
                    "The role " + actor.getRole() + " cannot perform this operation");
        }
        return actor;
    }
}
