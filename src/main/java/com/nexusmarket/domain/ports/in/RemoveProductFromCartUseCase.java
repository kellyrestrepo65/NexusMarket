package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * El carrito siempre pertenece al comprador identificado, asi que un comprador
 * nunca administra el carrito de otro (Dominio 2, RG-03).
 */
public interface RemoveProductFromCartUseCase {

    ShoppingCart execute(EntityId actorId, EntityId productId);
}
