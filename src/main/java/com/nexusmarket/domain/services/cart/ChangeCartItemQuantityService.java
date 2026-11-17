package com.nexusmarket.domain.services.cart;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ShoppingCartRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ChangeCartItemQuantityUseCase;
import org.springframework.stereotype.Service;

/**
 * El carrito siempre pertenece al comprador identificado, asi que un comprador
 * nunca administra el carrito de otro (Dominio 2, RG-03).
 */
@Service
public class ChangeCartItemQuantityService implements ChangeCartItemQuantityUseCase {

    private final AuthorizationService authorizationService;
    private final ShoppingCartRepositoryPort shoppingCartRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final CartStockValidator cartStockValidator;

    public ChangeCartItemQuantityService(AuthorizationService authorizationService,
                                         ShoppingCartRepositoryPort shoppingCartRepositoryPort,
                                         ProductRepositoryPort productRepositoryPort,
                                         CartStockValidator cartStockValidator) {
        this.productRepositoryPort = productRepositoryPort;
        this.cartStockValidator = cartStockValidator;
        this.authorizationService = authorizationService;
        this.shoppingCartRepositoryPort = shoppingCartRepositoryPort;
    }

    @Override
    public ShoppingCart execute(EntityId actorId, EntityId productId, int newQuantity) {
        User buyer = authorizationService.requireRole(actorId, Role.BUYER);
        ShoppingCart cart = shoppingCartRepositoryPort.findByBuyerId(buyer.getId())
                .orElseThrow(() -> new EntityNotFoundException("ShoppingCart for buyer", buyer.getId()));
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        cartStockValidator.validate(product, newQuantity);
        cart.changeQuantity(productId, newQuantity);
        return shoppingCartRepositoryPort.save(cart);
    }
}
