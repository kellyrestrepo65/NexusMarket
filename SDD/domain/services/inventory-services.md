# Servicios de Inventario

Paquete: `domain/services/inventory`. Objetivos: OBJ-05 (Dominio 6).

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| ReceiveStockService (ReceiveStockUseCase) | `execute(actorId, productId, warehouseId, quantity)` | movimiento Ingreso; solo productos fisicos; crea el inventario en el primer ingreso |
| AdjustInventoryService (AdjustInventoryUseCase) | `execute(actorId, inventoryId, delta)` | movimiento Ajuste; nunca negativo ni por debajo de lo reservado |
| MarkInventoryDamagedService (MarkInventoryDamagedUseCase) | `execute(actorId, inventoryId, quantity)` | las unidades dañadas nunca se reservan (seccion 11) |
| ConsultInventoryService (ConsultInventoryUseCase) | `execute(actorId, productId)` | el vendedor solo consulta sus productos; el comprador nunca |
| InventoryAccessValidator | auxiliar | Vendedor (productos y bodegas propios) u Operador Logistico |
