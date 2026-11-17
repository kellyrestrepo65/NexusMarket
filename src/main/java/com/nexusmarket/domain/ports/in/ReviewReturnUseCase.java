package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * OBJ-11 "Devoluciones": el Administrador aprueba o rechaza una devolucion
 * solicitada. Al aprobarla se crea un reembolso pendiente por el total de la
 * orden y las unidades fisicas vuelven como disponibles a la primera bodega del
 * producto.
 */
public interface ReviewReturnUseCase {

    Return approve(EntityId actorId, EntityId returnId);

    Return reject(EntityId actorId, EntityId returnId);
}
