package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.util.List;
import java.util.Optional;

public interface InventoryRepositoryPort {

    Inventory save(Inventory inventory);

    Optional<Inventory> findById(EntityId id);

    Optional<Inventory> findByProductIdAndWarehouseId(EntityId productId, EntityId warehouseId);

    List<Inventory> findByProductId(EntityId productId);

    List<Inventory> findAll();
}
