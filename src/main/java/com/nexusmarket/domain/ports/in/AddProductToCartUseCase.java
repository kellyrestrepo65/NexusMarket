package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Agrega un producto al carrito del comprador y crea el carrito la primera vez.
 * Solo compran los compradores con estado comercial habilitado (Dominio 2) y
 * solo se agregan productos publicados (Dominio 5). El inventario se reserva al
 * confirmar la orden (ver CheckoutService), no aqui.
 */
public interface AddProductToCartUseCase {

    ShoppingCart execute(EntityId actorId, EntityId productId, int quantity);
}
