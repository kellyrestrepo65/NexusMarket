package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.util.List;

/**
 * Un usuario consulta sus propios datos; el Administrador tambien a los
 * vendedores, y el Supervisor (perfil de consulta) a todos los usuarios.
 */
public interface ConsultUserUseCase {

    User execute(EntityId actorId, EntityId userId);

    List<User> executeAll(EntityId actorId);
}
