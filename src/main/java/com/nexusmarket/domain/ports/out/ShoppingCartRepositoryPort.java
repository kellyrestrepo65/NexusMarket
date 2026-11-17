package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.util.Optional;

public interface ShoppingCartRepositoryPort {

    ShoppingCart save(ShoppingCart cart);

    Optional<ShoppingCart> findByBuyerId(EntityId buyerId);
}
