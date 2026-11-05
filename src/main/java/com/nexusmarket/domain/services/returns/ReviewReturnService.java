package com.nexusmarket.domain.services.returns;

import com.nexusmarket.domain.models.user.Administrator;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.order.OrderLoader;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ReviewReturnUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OBJ-11 "Devoluciones": el Administrador aprueba o rechaza una devolucion
 * solicitada. Al aprobarla se crea un reembolso pendiente por el total de la
 * orden y las unidades fisicas vuelven como disponibles a la primera bodega del
 * producto.
 */
@Service
public class ReviewReturnService implements ReviewReturnUseCase {

    private final AuthorizationService authorizationService;
    private final ReturnLoader returnLoader;
    private final OrderLoader orderLoader;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public ReviewReturnService(AuthorizationService authorizationService, ReturnLoader returnLoader,
                               OrderLoader orderLoader, ReturnRepositoryPort returnRepositoryPort,
                               InventoryRepositoryPort inventoryRepositoryPort) {
        this.authorizationService = authorizationService;
        this.returnLoader = returnLoader;
        this.orderLoader = orderLoader;
        this.returnRepositoryPort = returnRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    @Transactional
    @Override
    public Return approve(EntityId actorId, EntityId returnId) {
        authorizationService.requireRole(actorId, Role.ADMINISTRATOR);
        Return orderReturn = returnLoader.load(returnId);
        Order order = orderLoader.load(orderReturn.getOrderId());
        orderReturn.approve(EntityId.next(), order.calculateTotal());
        for (OrderItem item : order.getItems()) {
            Product product = orderLoader.product(item);
            if (product.requiresPhysicalShipping()) {
                restock(product.getId(), item.getQuantity());
            }
        }
        return returnRepositoryPort.save(orderReturn);
    }

    @Override
    public Return reject(EntityId actorId, EntityId returnId) {
        authorizationService.requireRole(actorId, Role.ADMINISTRATOR);
        Return orderReturn = returnLoader.load(returnId);
        orderReturn.reject();
        return returnRepositoryPort.save(orderReturn);
    }

    private void restock(EntityId productId, int quantity) {
        inventoryRepositoryPort.findByProductId(productId).stream().findFirst().ifPresent(inventory -> {
            inventory.registerReturn(quantity);
            inventoryRepositoryPort.save(inventory);
        });
    }
}
