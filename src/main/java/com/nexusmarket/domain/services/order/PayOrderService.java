package com.nexusmarket.domain.services.order;

import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.Shipment;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.PayOrderUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Flujo 6.1 "Pago": el comprador paga su propia orden y se emite la factura
 * (OBJ-09). Una orden con productos fisicos recibe un envio en preparacion
 * (OBJ-10); una con solo productos digitales no necesita envio, asi que se
 * entrega de inmediato (Dominio 5).
 */
@Service
public class PayOrderService implements PayOrderUseCase {

    private final AuthorizationService authorizationService;
    private final OrderLoader orderLoader;
    private final OrderRepositoryPort orderRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ShipmentRepositoryPort shipmentRepositoryPort;

    public PayOrderService(AuthorizationService authorizationService, OrderLoader orderLoader,
                           OrderRepositoryPort orderRepositoryPort, InvoiceRepositoryPort invoiceRepositoryPort,
                           ShipmentRepositoryPort shipmentRepositoryPort) {
        this.authorizationService = authorizationService;
        this.orderLoader = orderLoader;
        this.orderRepositoryPort = orderRepositoryPort;
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.shipmentRepositoryPort = shipmentRepositoryPort;
    }

    @Transactional
    @Override
    public Order execute(EntityId actorId, EntityId orderId) {
        User buyer = authorizationService.requireRole(actorId, Role.BUYER);
        Order order = orderLoader.load(orderId);
        if (!order.getBuyerId().equals(buyer.getId())) {
            throw new ForbiddenOperationException("The order " + orderId + " belongs to another buyer");
        }
        order.advanceTo(OrderStatus.PAID);
        invoiceRepositoryPort.save(new Invoice(EntityId.next(), order.getId(), order.calculateTotal(),
                LocalDateTime.now()));
        if (orderLoader.isOnlyDigital(order)) {
            order.advanceTo(OrderStatus.DISPATCHED);
            order.advanceTo(OrderStatus.DELIVERED);
        } else {
            shipmentRepositoryPort.save(new Shipment(EntityId.next(), order.getId()));
        }
        return orderRepositoryPort.save(order);
    }
}
