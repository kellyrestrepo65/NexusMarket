package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Shipment;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.util.List;

/**
 * OBJ-10 "Procesos logisticos": el comprador sigue los envios de sus propias
 * ordenes; el Operador Logistico, el Administrador y el Supervisor ven todos los
 * envios.
 */
public interface ConsultShipmentsUseCase {

    List<Shipment> execute(EntityId actorId);

    Shipment executeForOrder(EntityId actorId, EntityId orderId);
}
