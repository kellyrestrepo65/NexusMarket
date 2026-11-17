package com.nexusmarket.domain.services.user;

import com.nexusmarket.domain.models.user.Administrator;
import com.nexusmarket.domain.models.user.Supervisor;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultUserUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Un usuario consulta sus propios datos; el Administrador tambien a los
 * vendedores, y el Supervisor (perfil de consulta) a todos los usuarios.
 */
@Service
public class ConsultUserService implements ConsultUserUseCase {

    private final AuthorizationService authorizationService;
    private final UserRepositoryPort userRepositoryPort;

    public ConsultUserService(AuthorizationService authorizationService,
                              UserRepositoryPort userRepositoryPort) {
        this.authorizationService = authorizationService;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(EntityId actorId, EntityId userId) {
        User actor = authorizationService.requireActiveUser(actorId);
        User target = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        if (actor.getRole() != Role.SUPERVISOR && !actor.canOperateOn(target)) {
            throw new ForbiddenOperationException("The user cannot consult data of user " + userId);
        }
        return target;
    }

    @Override
    public List<User> executeAll(EntityId actorId) {
        authorizationService.requireRole(actorId, Role.ADMINISTRATOR, Role.SUPERVISOR);
        return userRepositoryPort.findAll();
    }
}
