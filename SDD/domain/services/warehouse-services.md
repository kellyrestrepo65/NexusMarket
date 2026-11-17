# Servicios de Bodegas

Paquete: `domain/services/warehouse`. Objetivos: OBJ-03 (Dominio 4).

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| CreateWarehouseService (CreateWarehouseUseCase) | `execute(actorId, name, WarehouseType, sellerId)` | Administrador; el tipo SELLER exige un vendedor existente |
| ConsultWarehousesService (ConsultWarehousesUseCase) | `execute(actorId)` | el vendedor solo ve las suyas; Administrador, Operador Logistico y Supervisor ven todas |
