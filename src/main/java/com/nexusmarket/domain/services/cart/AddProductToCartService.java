package com.nexusmarket.domain.services.cart;

import com.nexusmarket.domain.services.order.CheckoutService;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.ShoppingCartRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.AddProductToCartUseCase;
import org.springframework.stereotype.Service;

/**
 * Agrega un producto al carrito del comprador y crea el carrito la primera vez.
 * Solo compran los compradores con estado comercial habilitado (Dominio 2) y
 * solo se agregan productos publicados (Dominio 5). El inventario se reserva al
 * confirmar la orden (ver CheckoutService), no aqui.
 */
@Service
public class AddProductToCartService implements AddProductToCartUseCase {

    private final AuthorizationService authorizationService;
    private final ProductRepositoryPort productRepositoryPort;
    private final ShoppingCartRepositoryPort shoppingCartRepositoryPort;

    public AddProductToCartService(AuthorizationService authorizationService,
                                   ProductRepositoryPort productRepositoryPort,
                                   ShoppingCartRepositoryPort shoppingCartRepositoryPort) {
        this.authorizationService = authorizationService;
        this.productRepositoryPort = productRepositoryPort;
        this.shoppingCartRepositoryPort = shoppingCartRepositoryPort;
    }

    @Override
    public ShoppingCart execute(EntityId actorId, EntityId productId, int quantity) {
        Buyer buyer = (Buyer) authorizationService.requireRole(actorId, Role.BUYER);
        if (!buyer.canPurchase()) {
            throw new BusinessRuleException("The buyer is not allowed to purchase");
        }
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (!product.isAvailableForPurchase()) {
            throw new BusinessRuleException("The product is not available for purchase");
        }
        ShoppingCart cart = shoppingCartRepositoryPort.findByBuyerId(buyer.getId())
                .orElseGet(() -> new ShoppingCart(EntityId.next(), buyer.getId()));
        cart.addProduct(productId, quantity);
        return shoppingCartRepositoryPort.save(cart);
    }
}
