package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Flujo 6.1 "Compra": cuando el comprador confirma el carrito se crea la orden,
 * se reserva el inventario de los productos fisicos (puede repartirse entre
 * varias bodegas) y la orden queda esperando el pago. Las unidades danadas nunca
 * se reservan (seccion 11) porque no hacen parte del stock libre. El precio se
 * toma del catalogo en el momento de la compra.
 */
public interface CheckoutUseCase {

    Order execute(EntityId actorId);
}
