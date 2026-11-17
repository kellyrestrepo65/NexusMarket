package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.util.List;

/**
 * Matriz de responsabilidades "Ordenes": el comprador ve sus propias ordenes, el
 * vendedor las ordenes que contienen sus productos, y el Operador Logistico y el
 * Supervisor todas las ordenes.
 */
public interface ConsultOrdersUseCase {

    List<Order> execute(EntityId actorId);

    Order executeOne(EntityId actorId, EntityId orderId);
}
