package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.application.adapters.out.persistence.mysql.entities.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentJpaRepository extends JpaRepository<ShipmentEntity, Integer> {

    Optional<ShipmentEntity> findByOrderId(Integer orderId);
}
