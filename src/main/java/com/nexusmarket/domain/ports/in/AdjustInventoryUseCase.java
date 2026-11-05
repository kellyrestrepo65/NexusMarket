package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Movement "Ajuste" (Domain 6). The stock can never become negative.
 */
public interface AdjustInventoryUseCase {

    Inventory execute(EntityId actorId, EntityId inventoryId, int delta);
}
