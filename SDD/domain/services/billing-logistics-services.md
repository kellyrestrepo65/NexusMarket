# Servicios de Facturacion y logistica

Paquete: `domain/services/billing, domain/services/logistics`. Objetivos: OBJ-09, OBJ-10.

Facturas y envios se generan en el flujo de la orden (pago y despacho); estos servicios solo los consultan.

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| ConsultInvoicesService (ConsultInvoicesUseCase) | `execute(actorId)`, `executeForOrder(actorId, orderId)` | comprador: sus ordenes; Administrador y Supervisor: todas |
| ConsultShipmentsService (ConsultShipmentsUseCase) | `execute(actorId)`, `executeForOrder(actorId, orderId)` | comprador: sus ordenes; Operador Logistico, Administrador y Supervisor: todos |
