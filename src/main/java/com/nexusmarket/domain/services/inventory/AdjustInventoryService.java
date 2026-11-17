package com.nexusmarket.domain.services.inventory;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.in.AdjustInventoryUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Movement "Ajuste" (Domain 6). The stock can never become negative.
 */
@Service
public class AdjustInventoryService implements AdjustInventoryUseCase {

    private final InventoryAccessValidator inventoryAccessValidator;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public AdjustInventoryService(InventoryAccessValidator inventoryAccessValidator,
                                  InventoryRepositoryPort inventoryRepositoryPort) {
        this.inventoryAccessValidator = inventoryAccessValidator;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    @Transactional
    @Override
    public Inventory execute(EntityId actorId, EntityId inventoryId, int delta) {
        Inventory inventory = inventoryRepositoryPort.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory", inventoryId));
        inventoryAccessValidator.validate(actorId, inventory.getProductId(), inventory.getWarehouseId());
        inventory.adjust(delta);
        return inventoryRepositoryPort.save(inventory);
    }
}
