package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Flujo 6.1 "Entrega": el Operador Logistico confirma la entrega. Una orden
 * entregada es final y no se puede modificar (seccion 11); su envio tambien
 * queda entregado (OBJ-10).
 */
public interface DeliverOrderUseCase {

    Order execute(EntityId actorId, EntityId orderId);
}
