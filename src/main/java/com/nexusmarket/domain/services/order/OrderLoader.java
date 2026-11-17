package com.nexusmarket.domain.services.order;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Component;

/**
 * Carga ordenes y responde si una orden solo contiene productos digitales, que
 * se entregan justo despues del pago (Dominio 5).
 */
@Component
public class OrderLoader {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    public OrderLoader(OrderRepositoryPort orderRepositoryPort, ProductRepositoryPort productRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
    }

    public Order load(EntityId orderId) {
        return orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
    }

    public Product product(OrderItem item) {
        return productRepositoryPort.findById(item.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product", item.getProductId()));
    }

    public boolean isOnlyDigital(Order order) {
        return order.getItems().stream().noneMatch(item -> product(item).requiresPhysicalShipping());
    }
}
