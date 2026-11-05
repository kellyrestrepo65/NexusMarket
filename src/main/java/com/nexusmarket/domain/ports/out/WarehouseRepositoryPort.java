package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepositoryPort {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findById(EntityId id);

    List<Warehouse> findByOwnerId(EntityId ownerId);

    List<Warehouse> findAll();
}
