package com.nexusmarket.domain.services.inventory;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.in.MarkInventoryDamagedUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seccion 11: el inventario marcado como "Danado" no se puede reservar, asi que
 * las unidades danadas salen del stock libre.
 */
@Service
public class MarkInventoryDamagedService implements MarkInventoryDamagedUseCase {

    private final InventoryAccessValidator inventoryAccessValidator;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public MarkInventoryDamagedService(InventoryAccessValidator inventoryAccessValidator,
                                       InventoryRepositoryPort inventoryRepositoryPort) {
        this.inventoryAccessValidator = inventoryAccessValidator;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    @Transactional
    @Override
    public Inventory execute(EntityId actorId, EntityId inventoryId, int quantity) {
        Inventory inventory = inventoryRepositoryPort.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory", inventoryId));
        inventoryAccessValidator.validate(actorId, inventory.getProductId(), inventory.getWarehouseId());
        inventory.markAsDamaged(quantity);
        return inventoryRepositoryPort.save(inventory);
    }
}
