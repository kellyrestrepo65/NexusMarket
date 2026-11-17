package com.nexusmarket.domain.services.returns;

import com.nexusmarket.domain.models.user.Administrator;
import com.nexusmarket.domain.models.user.Supervisor;

import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.order.OrderLoader;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultReturnsUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OBJ-11: el comprador sigue sus propias devoluciones y reembolsos; el
 * Administrador y el Supervisor los ven todos.
 */
@Service
public class ConsultReturnsService implements ConsultReturnsUseCase {

    private final AuthorizationService authorizationService;
    private final ReturnLoader returnLoader;
    private final OrderLoader orderLoader;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;

    public ConsultReturnsService(AuthorizationService authorizationService, ReturnLoader returnLoader,
                                 OrderLoader orderLoader, OrderRepositoryPort orderRepositoryPort,
                                 ReturnRepositoryPort returnRepositoryPort) {
        this.authorizationService = authorizationService;
        this.returnLoader = returnLoader;
        this.orderLoader = orderLoader;
        this.orderRepositoryPort = orderRepositoryPort;
        this.returnRepositoryPort = returnRepositoryPort;
    }

    @Override
    public List<Return> execute(EntityId actorId) {
        User actor = authorizationService.requireRole(actorId, Role.BUYER, Role.ADMINISTRATOR, Role.SUPERVISOR);
        if (actor.getRole() != Role.BUYER) {
            return returnRepositoryPort.findAll();
        }
        Set<EntityId> ownOrders = orderRepositoryPort.findByBuyerId(actor.getId()).stream()
                .map(Order::getId)
                .collect(Collectors.toSet());
        return returnRepositoryPort.findAll().stream()
                .filter(orderReturn -> ownOrders.contains(orderReturn.getOrderId()))
                .toList();
    }

    @Override
    public Return executeOne(EntityId actorId, EntityId returnId) {
        User actor = authorizationService.requireRole(actorId, Role.BUYER, Role.ADMINISTRATOR, Role.SUPERVISOR);
        Return orderReturn = returnLoader.load(returnId);
        if (actor.getRole() == Role.BUYER
                && !orderLoader.load(orderReturn.getOrderId()).getBuyerId().equals(actor.getId())) {
            throw new ForbiddenOperationException("The user cannot consult return " + returnId);
        }
        return orderReturn;
    }
}
