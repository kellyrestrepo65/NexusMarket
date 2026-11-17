package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Seccion 11: el inventario marcado como "Danado" no se puede reservar, asi que
 * las unidades danadas salen del stock libre.
 */
public interface MarkInventoryDamagedUseCase {

    Inventory execute(EntityId actorId, EntityId inventoryId, int quantity);
}
