package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.util.List;

/**
 * OBJ-11: el comprador sigue sus propias devoluciones y reembolsos; el
 * Administrador y el Supervisor los ven todos.
 */
public interface ConsultReturnsUseCase {

    List<Return> execute(EntityId actorId);

    Return executeOne(EntityId actorId, EntityId returnId);
}
