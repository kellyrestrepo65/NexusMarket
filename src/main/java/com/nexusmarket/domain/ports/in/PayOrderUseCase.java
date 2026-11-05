package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Flujo 6.1 "Pago": el comprador paga su propia orden y se emite la factura
 * (OBJ-09). Una orden con productos fisicos recibe un envio en preparacion
 * (OBJ-10); una con solo productos digitales no necesita envio, asi que se
 * entrega de inmediato (Dominio 5).
 */
public interface PayOrderUseCase {

    Order execute(EntityId actorId, EntityId orderId);
}
