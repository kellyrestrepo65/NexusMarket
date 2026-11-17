package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.util.List;

/**
 * Un vendedor solo ve sus propias bodegas; el Administrador, el Operador
 * Logistico y el Supervisor las ven todas.
 */
public interface ConsultWarehousesUseCase {

    List<Warehouse> execute(EntityId actorId);
}
