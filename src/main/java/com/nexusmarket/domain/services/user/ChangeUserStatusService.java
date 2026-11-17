package com.nexusmarket.domain.services.user;

import com.nexusmarket.domain.models.user.Administrator;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ChangeUserStatusUseCase;
import org.springframework.stereotype.Service;

/**
 * Proceso "Administracion de usuarios" (Dominio 1, estado Activo/Bloqueado).
 * El Administrador gestiona a los vendedores, asi que User.canOperateOn
 * decide a que usuarios puede bloquear o activar.
 */
@Service
public class ChangeUserStatusService implements ChangeUserStatusUseCase {

    private final AuthorizationService authorizationService;
    private final UserRepositoryPort userRepositoryPort;

    public ChangeUserStatusService(AuthorizationService authorizationService,
                                   UserRepositoryPort userRepositoryPort) {
        this.authorizationService = authorizationService;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(EntityId actorId, EntityId userId, boolean active) {
        User admin = authorizationService.requireRole(actorId, Role.ADMINISTRATOR);
        User target = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        if (admin.equals(target) || !admin.canOperateOn(target)) {
            throw new ForbiddenOperationException("The administrator cannot change the status of user " + userId);
        }
        if (active) {
            target.activate();
        } else {
            target.block();
        }
        return userRepositoryPort.save(target);
    }
}
