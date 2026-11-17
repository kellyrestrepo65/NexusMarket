package com.nexusmarket.domain.services.order;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.DispatchOrderUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Flujo 6.1 "Despacho": el Operador Logistico despacha una orden pagada. Las
 * unidades reservadas de cada producto fisico salen de las bodegas y el envio,
 * ahora asignado a ese operador, pasa a en transito (OBJ-10).
 */
@Service
public class DispatchOrderService implements DispatchOrderUseCase {

    private final AuthorizationService authorizationService;
    private final OrderLoader orderLoader;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ShipmentRepositoryPort shipmentRepositoryPort;

    public DispatchOrderService(AuthorizationService authorizationService, OrderLoader orderLoader,
                                InventoryRepositoryPort inventoryRepositoryPort,
                                OrderRepositoryPort orderRepositoryPort,
                                ShipmentRepositoryPort shipmentRepositoryPort) {
        this.authorizationService = authorizationService;
        this.orderLoader = orderLoader;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.shipmentRepositoryPort = shipmentRepositoryPort;
    }

    @Transactional
    @Override
    public Order execute(EntityId actorId, EntityId orderId) {
        User operator = authorizationService.requireRole(actorId, Role.LOGISTICS_OPERATOR);
        Order order = orderLoader.load(orderId);
        order.advanceTo(OrderStatus.DISPATCHED);
        for (OrderItem item : order.getItems()) {
            Product product = orderLoader.product(item);
            if (product.requiresPhysicalShipping()) {
                confirmDispatch(product.getId(), item.getQuantity());
            }
        }
        shipmentRepositoryPort.findByOrderId(orderId).ifPresent(shipment -> {
            shipment.assignOperator(operator.getId());
            shipment.markInTransit();
            shipmentRepositoryPort.save(shipment);
        });
        return orderRepositoryPort.save(order);
    }

    private void confirmDispatch(EntityId productId, int quantity) {
        int pending = quantity;
        for (Inventory inventory : inventoryRepositoryPort.findByProductId(productId)) {
            int take = Math.min(pending, inventory.getReservedQuantity());
            if (take > 0) {
                inventory.confirmDispatch(take);
                inventoryRepositoryPort.save(inventory);
                pending -= take;
            }
            if (pending == 0) {
                return;
            }
        }
    }
}
