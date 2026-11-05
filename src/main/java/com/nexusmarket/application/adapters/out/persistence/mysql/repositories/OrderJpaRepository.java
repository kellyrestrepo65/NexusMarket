package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.application.adapters.out.persistence.mysql.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Integer> {

    List<OrderEntity> findByBuyerId(Integer buyerId);
}
