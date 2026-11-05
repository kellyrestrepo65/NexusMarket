package com.nexusmarket.domain.services.order;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.DeliverOrderUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Flujo 6.1 "Entrega": el Operador Logistico confirma la entrega. Una orden
 * entregada es final y no se puede modificar (seccion 11); su envio tambien
 * queda entregado (OBJ-10).
 */
@Service
public class DeliverOrderService implements DeliverOrderUseCase {

    private final AuthorizationService authorizationService;
    private final OrderLoader orderLoader;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ShipmentRepositoryPort shipmentRepositoryPort;

    public DeliverOrderService(AuthorizationService authorizationService, OrderLoader orderLoader,
                               OrderRepositoryPort orderRepositoryPort,
                               ShipmentRepositoryPort shipmentRepositoryPort) {
        this.authorizationService = authorizationService;
        this.orderLoader = orderLoader;
        this.orderRepositoryPort = orderRepositoryPort;
        this.shipmentRepositoryPort = shipmentRepositoryPort;
    }

    @Transactional
    @Override
    public Order execute(EntityId actorId, EntityId orderId) {
        authorizationService.requireRole(actorId, Role.LOGISTICS_OPERATOR);
        Order order = orderLoader.load(orderId);
        order.advanceTo(OrderStatus.DELIVERED);
        shipmentRepositoryPort.findByOrderId(orderId).ifPresent(shipment -> {
            shipment.markDelivered();
            shipmentRepositoryPort.save(shipment);
        });
        return orderRepositoryPort.save(order);
    }
}
