package com.nexusmarket.domain.services.order;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.models.cart.CartItem;
import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.ShoppingCartRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.CheckoutUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Flujo 6.1 "Compra": cuando el comprador confirma el carrito se crea la orden,
 * se reserva el inventario de los productos fisicos (puede repartirse entre
 * varias bodegas) y la orden queda esperando el pago. Las unidades danadas nunca
 * se reservan (seccion 11) porque no hacen parte del stock libre. El precio se
 * toma del catalogo en el momento de la compra.
 */
@Service
public class CheckoutService implements CheckoutUseCase {

    private final AuthorizationService authorizationService;
    private final ShoppingCartRepositoryPort shoppingCartRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    public CheckoutService(AuthorizationService authorizationService,
                           ShoppingCartRepositoryPort shoppingCartRepositoryPort,
                           ProductRepositoryPort productRepositoryPort,
                           InventoryRepositoryPort inventoryRepositoryPort,
                           OrderRepositoryPort orderRepositoryPort) {
        this.authorizationService = authorizationService;
        this.shoppingCartRepositoryPort = shoppingCartRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Transactional
    @Override
    public Order execute(EntityId actorId) {
        Buyer buyer = (Buyer) authorizationService.requireRole(actorId, Role.BUYER);
        if (!buyer.canPurchase()) {
            throw new BusinessRuleException("The buyer is not allowed to purchase");
        }
        ShoppingCart cart = shoppingCartRepositoryPort.findByBuyerId(buyer.getId())
                .orElseThrow(() -> new EntityNotFoundException("ShoppingCart for buyer", buyer.getId()));
        if (cart.isEmpty()) {
            throw new BusinessRuleException("The cart is empty");
        }

        Order order = new Order(EntityId.next(), buyer.getId());
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepositoryPort.findById(cartItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product", cartItem.getProductId()));
            if (!product.isAvailableForPurchase()) {
                throw new BusinessRuleException("The product " + product.getId() + " is no longer available");
            }
            if (product.requiresPhysicalShipping()) {
                reserve(product.getId(), cartItem.getQuantity());
            }
            order.addItem(new OrderItem(product.getId(), cartItem.getQuantity(), product.getPrice()));
        }
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        Order saved = orderRepositoryPort.save(order);

        cart.clear();
        shoppingCartRepositoryPort.save(cart);
        return saved;
    }

    private void reserve(EntityId productId, int quantity) {
        List<Inventory> inventories = inventoryRepositoryPort.findByProductId(productId);
        int free = inventories.stream().mapToInt(Inventory::getAvailableFreeQuantity).sum();
        if (free < quantity) {
            throw new BusinessRuleException("Insufficient stock for product " + productId);
        }
        int pending = quantity;
        for (Inventory inventory : inventories) {
            int take = Math.min(pending, inventory.getAvailableFreeQuantity());
            if (take > 0) {
                inventory.reserve(take);
                inventoryRepositoryPort.save(inventory);
                pending -= take;
            }
            if (pending == 0) {
                return;
            }
        }
    }
}
