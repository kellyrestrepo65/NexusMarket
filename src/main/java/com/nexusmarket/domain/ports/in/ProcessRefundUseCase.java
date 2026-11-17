package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * OBJ-11 "Reembolsos": el Administrador procesa o rechaza el reembolso pendiente
 * de una devolucion aprobada.
 */
public interface ProcessRefundUseCase {

    Return process(EntityId actorId, EntityId returnId);

    Return reject(EntityId actorId, EntityId returnId);
}
