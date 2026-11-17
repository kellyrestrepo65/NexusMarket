package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Proceso "Administracion de usuarios" (Dominio 1, estado Activo/Bloqueado).
 * El Administrador gestiona a los vendedores, asi que User.canOperateOn
 * decide a que usuarios puede bloquear o activar.
 */
public interface ChangeUserStatusUseCase {

    User execute(EntityId actorId, EntityId userId, boolean active);
}
