package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.util.List;

/**
 * OBJ-09 "Facturacion": el comprador consulta las facturas de sus propias
 * ordenes; el Administrador y el Supervisor consultan todas las facturas.
 */
public interface ConsultInvoicesUseCase {

    List<Invoice> execute(EntityId actorId);

    Invoice executeForOrder(EntityId actorId, EntityId orderId);
}
