package com.nexusmarket.domain.services.logistics;

import com.nexusmarket.domain.models.user.Administrator;
import com.nexusmarket.domain.models.user.Supervisor;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.Shipment;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.services.order.OrderLoader;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultShipmentsUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OBJ-10 "Procesos logisticos": el comprador sigue los envios de sus propias
 * ordenes; el Operador Logistico, el Administrador y el Supervisor ven todos los
 * envios.
 */
@Service
public class ConsultShipmentsService implements ConsultShipmentsUseCase {

    private final AuthorizationService authorizationService;
    private final OrderLoader orderLoader;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ShipmentRepositoryPort shipmentRepositoryPort;

    public ConsultShipmentsService(AuthorizationService authorizationService, OrderLoader orderLoader,
                                   OrderRepositoryPort orderRepositoryPort,
                                   ShipmentRepositoryPort shipmentRepositoryPort) {
        this.authorizationService = authorizationService;
        this.orderLoader = orderLoader;
        this.orderRepositoryPort = orderRepositoryPort;
        this.shipmentRepositoryPort = shipmentRepositoryPort;
    }

    @Override
    public List<Shipment> execute(EntityId actorId) {
        User actor = authorizationService.requireRole(actorId,
                Role.BUYER, Role.LOGISTICS_OPERATOR, Role.ADMINISTRATOR, Role.SUPERVISOR);
        if (actor.getRole() != Role.BUYER) {
            return shipmentRepositoryPort.findAll();
        }
        Set<EntityId> ownOrders = orderRepositoryPort.findByBuyerId(actor.getId()).stream()
                .map(Order::getId)
                .collect(Collectors.toSet());
        return shipmentRepositoryPort.findAll().stream()
                .filter(shipment -> ownOrders.contains(shipment.getOrderId()))
                .toList();
    }

    @Override
    public Shipment executeForOrder(EntityId actorId, EntityId orderId) {
        User actor = authorizationService.requireRole(actorId,
                Role.BUYER, Role.LOGISTICS_OPERATOR, Role.ADMINISTRATOR, Role.SUPERVISOR);
        Order order = orderLoader.load(orderId);
        if (actor.getRole() == Role.BUYER && !order.getBuyerId().equals(actor.getId())) {
            throw new ForbiddenOperationException("The user cannot consult the shipment of order " + orderId);
        }
        return shipmentRepositoryPort.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Shipment of order", orderId));
    }
}
