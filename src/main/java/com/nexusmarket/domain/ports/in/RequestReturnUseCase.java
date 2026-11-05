package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * OBJ-11 "Devoluciones": el comprador solicita la devolucion de una de sus
 * propias ordenes entregadas. Una orden acepta una sola solicitud de devolucion.
 */
public interface RequestReturnUseCase {

    Return execute(EntityId actorId, EntityId orderId, String reason);
}
