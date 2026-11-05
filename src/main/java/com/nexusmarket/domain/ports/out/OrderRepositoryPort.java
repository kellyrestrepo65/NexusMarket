package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(EntityId id);

    List<Order> findByBuyerId(EntityId buyerId);

    List<Order> findAll();
}
