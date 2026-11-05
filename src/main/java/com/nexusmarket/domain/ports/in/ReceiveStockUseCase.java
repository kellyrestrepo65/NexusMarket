package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Movimiento "Ingreso" (Dominio 6). El inventario siempre esta ligado a un
 * producto y a una bodega y se crea con el primer ingreso. Solo los productos
 * fisicos tienen inventario (Dominio 5).
 */
public interface ReceiveStockUseCase {

    Inventory execute(EntityId actorId, EntityId productId, EntityId warehouseId, int quantity);
}
