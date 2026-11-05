package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.util.List;

/**
 * Inventario distribuido de un producto entre bodegas. El comprador nunca
 * consulta inventarios (Dominio 2).
 */
public interface ConsultInventoryUseCase {

    List<Inventory> execute(EntityId actorId, EntityId productId);
}
