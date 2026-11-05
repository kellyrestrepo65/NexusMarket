package com.nexusmarket.domain.services.cart;

import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ShoppingCartRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultCartUseCase;
import org.springframework.stereotype.Service;

/**
 * El carrito siempre pertenece al comprador identificado, asi que un comprador
 * nunca administra el carrito de otro (Dominio 2, RG-03). Un comprador que nunca
 * agrego nada recibe un carrito vacio.
 */
@Service
public class ConsultCartService implements ConsultCartUseCase {

    private final AuthorizationService authorizationService;
    private final ShoppingCartRepositoryPort shoppingCartRepositoryPort;

    public ConsultCartService(AuthorizationService authorizationService,
                              ShoppingCartRepositoryPort shoppingCartRepositoryPort) {
        this.authorizationService = authorizationService;
        this.shoppingCartRepositoryPort = shoppingCartRepositoryPort;
    }

    @Override
    public ShoppingCart execute(EntityId actorId) {
        User buyer = authorizationService.requireRole(actorId, Role.BUYER);
        return shoppingCartRepositoryPort.findByBuyerId(buyer.getId())
                .orElseGet(() -> new ShoppingCart(EntityId.next(), buyer.getId()));
    }
}
