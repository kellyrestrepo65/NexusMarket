package com.nexusmarket.application.adapters.out.persistence.mysql.adapters;

import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.ShoppingCartRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.ShoppingCartEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.ShoppingCartJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShoppingCartMysqlAdapter implements ShoppingCartRepositoryPort {

    private final ShoppingCartJpaRepository shoppingCartJpaRepository;

    public ShoppingCartMysqlAdapter(ShoppingCartJpaRepository shoppingCartJpaRepository) {
        this.shoppingCartJpaRepository = shoppingCartJpaRepository;
    }

    @Override
    public ShoppingCart save(ShoppingCart cart) {
        shoppingCartJpaRepository.save(ShoppingCartEntity.from(cart));
        return cart;
    }

    @Override
    public Optional<ShoppingCart> findByBuyerId(EntityId buyerId) {
        return shoppingCartJpaRepository.findByBuyerId(buyerId.value()).map(ShoppingCartEntity::toDomain);
    }
}
