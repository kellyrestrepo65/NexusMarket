package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * OBJ-11 "Devoluciones": el comprador solicita la devolucion de una de sus
 * propias ordenes entregadas. Una orden acepta una sola solicitud de devolucion.
 */
public interface RequestReturnUseCase {

    /** Devolucion de un solo producto de la orden (productId obligatorio). */
    Return execute(EntityId actorId, EntityId orderId, EntityId productId, String reason);
}
