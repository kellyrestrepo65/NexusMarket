package com.nexusmarket.domain.services.order;

import com.nexusmarket.domain.models.user.Supervisor;

import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultOrdersUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Matriz de responsabilidades "Ordenes": el comprador ve sus propias ordenes, el
 * vendedor las ordenes que contienen sus productos, y el Operador Logistico y el
 * Supervisor todas las ordenes.
 */
@Service
public class ConsultOrdersService implements ConsultOrdersUseCase {

    private final AuthorizationService authorizationService;
    private final OrderLoader orderLoader;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    public ConsultOrdersService(AuthorizationService authorizationService, OrderLoader orderLoader,
                                OrderRepositoryPort orderRepositoryPort,
                                ProductRepositoryPort productRepositoryPort) {
        this.authorizationService = authorizationService;
        this.orderLoader = orderLoader;
        this.orderRepositoryPort = orderRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public List<Order> execute(EntityId actorId) {
        User actor = authorizationService.requireRole(actorId,
                Role.BUYER, Role.SELLER, Role.LOGISTICS_OPERATOR, Role.SUPERVISOR);
        if (actor.getRole() == Role.BUYER) {
            return orderRepositoryPort.findByBuyerId(actor.getId());
        }
        if (actor.getRole() == Role.SELLER) {
            Set<EntityId> ownProducts = ownProducts(actor);
            return orderRepositoryPort.findAll().stream()
                    .filter(order -> containsAny(order, ownProducts))
                    .toList();
        }
        return orderRepositoryPort.findAll();
    }

    @Override
    public Order executeOne(EntityId actorId, EntityId orderId) {
        User actor = authorizationService.requireRole(actorId,
                Role.BUYER, Role.SELLER, Role.LOGISTICS_OPERATOR, Role.SUPERVISOR);
        Order order = orderLoader.load(orderId);
        boolean allowed = switch (actor.getRole()) {
            case BUYER -> order.getBuyerId().equals(actor.getId());
            case SELLER -> containsAny(order, ownProducts(actor));
            default -> true;
        };
        if (!allowed) {
            throw new ForbiddenOperationException("The user cannot consult order " + orderId);
        }
        return order;
    }

    private Set<EntityId> ownProducts(User seller) {
        return productRepositoryPort.findBySellerId(seller.getId()).stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
    }

    private boolean containsAny(Order order, Set<EntityId> productIds) {
        return order.getItems().stream().anyMatch(item -> productIds.contains(item.getProductId()));
    }
}
