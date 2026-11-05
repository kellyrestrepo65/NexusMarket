package com.nexusmarket.domain.services.billing;

import com.nexusmarket.domain.models.user.Administrator;
import com.nexusmarket.domain.models.user.Supervisor;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.services.order.OrderLoader;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultInvoicesUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OBJ-09 "Facturacion": el comprador consulta las facturas de sus propias
 * ordenes; el Administrador y el Supervisor consultan todas las facturas.
 */
@Service
public class ConsultInvoicesService implements ConsultInvoicesUseCase {

    private final AuthorizationService authorizationService;
    private final OrderLoader orderLoader;
    private final OrderRepositoryPort orderRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public ConsultInvoicesService(AuthorizationService authorizationService, OrderLoader orderLoader,
                                  OrderRepositoryPort orderRepositoryPort,
                                  InvoiceRepositoryPort invoiceRepositoryPort) {
        this.authorizationService = authorizationService;
        this.orderLoader = orderLoader;
        this.orderRepositoryPort = orderRepositoryPort;
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public List<Invoice> execute(EntityId actorId) {
        User actor = authorizationService.requireRole(actorId, Role.BUYER, Role.ADMINISTRATOR, Role.SUPERVISOR);
        if (actor.getRole() != Role.BUYER) {
            return invoiceRepositoryPort.findAll();
        }
        Set<EntityId> ownOrders = orderRepositoryPort.findByBuyerId(actor.getId()).stream()
                .map(Order::getId)
                .collect(Collectors.toSet());
        return invoiceRepositoryPort.findAll().stream()
                .filter(invoice -> ownOrders.contains(invoice.getOrderId()))
                .toList();
    }

    @Override
    public Invoice executeForOrder(EntityId actorId, EntityId orderId) {
        User actor = authorizationService.requireRole(actorId, Role.BUYER, Role.ADMINISTRATOR, Role.SUPERVISOR);
        Order order = orderLoader.load(orderId);
        if (actor.getRole() == Role.BUYER && !order.getBuyerId().equals(actor.getId())) {
            throw new ForbiddenOperationException("The user cannot consult the invoice of order " + orderId);
        }
        return invoiceRepositoryPort.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice of order", orderId));
    }
}
