package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Flujo 6.1 "Despacho": el Operador Logistico despacha una orden pagada. Las
 * unidades reservadas de cada producto fisico salen de las bodegas y el envio,
 * ahora asignado a ese operador, pasa a en transito (OBJ-10).
 */
public interface DispatchOrderUseCase {

    Order execute(EntityId actorId, EntityId orderId);
}
