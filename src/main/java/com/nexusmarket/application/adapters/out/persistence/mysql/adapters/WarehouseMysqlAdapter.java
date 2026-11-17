package com.nexusmarket.application.adapters.out.persistence.mysql.adapters;

import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WarehouseMysqlAdapter implements WarehouseRepositoryPort {

    private final WarehouseJpaRepository warehouseJpaRepository;

    public WarehouseMysqlAdapter(WarehouseJpaRepository warehouseJpaRepository) {
        this.warehouseJpaRepository = warehouseJpaRepository;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        warehouseJpaRepository.save(WarehouseEntity.from(warehouse));
        return warehouse;
    }

    @Override
    public Optional<Warehouse> findById(EntityId id) {
        return warehouseJpaRepository.findById(id.value()).map(WarehouseEntity::toDomain);
    }

    @Override
    public List<Warehouse> findByOwnerId(EntityId ownerId) {
        return warehouseJpaRepository.findByOwnerIdOrderByIdAsc(ownerId.value()).stream()
                .map(WarehouseEntity::toDomain).toList();
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouseJpaRepository.findAll().stream().map(WarehouseEntity::toDomain).toList();
    }
}
