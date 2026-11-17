package com.nexusmarket.application.adapters.out.persistence.mysql.adapters;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.OrderEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderMysqlAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;

    public OrderMysqlAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        orderJpaRepository.save(OrderEntity.from(order));
        return order;
    }

    @Override
    public Optional<Order> findById(EntityId id) {
        return orderJpaRepository.findById(id.value()).map(OrderEntity::toDomain);
    }

    @Override
    public List<Order> findByBuyerId(EntityId buyerId) {
        return orderJpaRepository.findByBuyerId(buyerId.value()).stream().map(OrderEntity::toDomain).toList();
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll().stream().map(OrderEntity::toDomain).toList();
    }
}
