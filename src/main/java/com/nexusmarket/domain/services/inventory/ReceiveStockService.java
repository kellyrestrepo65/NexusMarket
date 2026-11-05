package com.nexusmarket.domain.services.inventory;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.in.ReceiveStockUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Movimiento "Ingreso" (Dominio 6). El inventario siempre esta ligado a un
 * producto y a una bodega y se crea con el primer ingreso. Solo los productos
 * fisicos tienen inventario (Dominio 5).
 */
@Service
public class ReceiveStockService implements ReceiveStockUseCase {

    private final InventoryAccessValidator inventoryAccessValidator;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public ReceiveStockService(InventoryAccessValidator inventoryAccessValidator,
                               InventoryRepositoryPort inventoryRepositoryPort) {
        this.inventoryAccessValidator = inventoryAccessValidator;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    @Transactional
    @Override
    public Inventory execute(EntityId actorId, EntityId productId, EntityId warehouseId, int quantity) {
        Product product = inventoryAccessValidator.validate(actorId, productId, warehouseId);
        if (!product.requiresPhysicalShipping()) {
            throw new BusinessRuleException("Digital products do not have inventory");
        }
        Inventory inventory = inventoryRepositoryPort.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> new Inventory(EntityId.next(), productId, warehouseId));
        inventory.receiveStock(quantity);
        return inventoryRepositoryPort.save(inventory);
    }
}
