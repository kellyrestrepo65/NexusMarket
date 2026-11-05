package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.application.adapters.out.persistence.mysql.entities.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryJpaRepository extends JpaRepository<InventoryEntity, Integer> {

    Optional<InventoryEntity> findByProductIdAndWarehouseId(Integer productId, Integer warehouseId);

    List<InventoryEntity> findByProductIdOrderByIdAsc(Integer productId);
}
