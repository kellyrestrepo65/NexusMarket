# Servicios de Ordenes

Paquete: `domain/services/order`. Objetivos: OBJ-07, OBJ-08 (flujo 6.1).

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| CheckoutService (CheckoutUseCase) | `execute(actorId)` | reserva inventario de cada item fisico, repartido entre bodegas (falla si no hay stock libre); orden → PENDING_PAYMENT; precio del catalogo; vacia el carrito |
| PayOrderService (PayOrderUseCase) | `execute(actorId, orderId)` | solo el dueño; → PAID; crea la factura; crea el envio si hay productos fisicos, si no → DELIVERED |
| DispatchOrderService (DispatchOrderUseCase) | `execute(actorId, orderId)` | Operador Logistico; salida por venta del stock reservado; envio IN_TRANSIT |
| DeliverOrderService (DeliverOrderUseCase) | `execute(actorId, orderId)` | Operador Logistico; → DELIVERED (final); envio entregado |
| ConsultOrdersService (ConsultOrdersUseCase) | `execute(actorId)`, `executeOne(actorId, orderId)` | comprador: las suyas; vendedor: las que tienen sus productos; Operador Logistico y Supervisor: todas |
| OrderLoader | auxiliar | carga orden y productos; `isOnlyDigital` |
