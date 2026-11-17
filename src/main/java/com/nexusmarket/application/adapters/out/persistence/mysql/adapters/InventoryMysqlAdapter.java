package com.nexusmarket.application.adapters.out.persistence.mysql.adapters;

import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.InventoryEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.InventoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Una fila existente se actualiza en su lugar (nunca se reemplaza) para que se
 * verifique su version: un cambio concurrente hace fallar el guardado en vez de
 * perder stock.
 */
@Component
public class InventoryMysqlAdapter implements InventoryRepositoryPort {

    private final InventoryJpaRepository inventoryJpaRepository;

    public InventoryMysqlAdapter(InventoryJpaRepository inventoryJpaRepository) {
        this.inventoryJpaRepository = inventoryJpaRepository;
    }

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = inventoryJpaRepository.findById(inventory.getId().value())
                .orElseGet(() -> InventoryEntity.create(inventory));
        entity.copyQuantities(inventory);
        return inventoryJpaRepository.saveAndFlush(entity).toDomain();
    }

    @Override
    public Optional<Inventory> findById(EntityId id) {
        return inventoryJpaRepository.findById(id.value()).map(InventoryEntity::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductIdAndWarehouseId(EntityId productId, EntityId warehouseId) {
        return inventoryJpaRepository.findByProductIdAndWarehouseId(productId.value(), warehouseId.value())
                .map(InventoryEntity::toDomain);
    }

    @Override
    public List<Inventory> findByProductId(EntityId productId) {
        return inventoryJpaRepository.findByProductIdOrderByIdAsc(productId.value()).stream()
                .map(InventoryEntity::toDomain).toList();
    }

    @Override
    public List<Inventory> findAll() {
        return inventoryJpaRepository.findAll().stream().map(InventoryEntity::toDomain).toList();
    }
}
