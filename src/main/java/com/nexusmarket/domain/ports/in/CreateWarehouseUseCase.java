package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.enums.WarehouseType;
import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Dominio 4: el Administrador gestiona las bodegas, ya sean del Marketplace o de
 * un vendedor.
 */
public interface CreateWarehouseUseCase {

    Warehouse execute(EntityId actorId, String name, WarehouseType type, EntityId sellerId);
}
