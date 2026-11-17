# Servicios de Devoluciones y reembolsos

Paquete: `domain/services/returns`. Objetivos: OBJ-11.

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| RequestReturnService (RequestReturnUseCase) | `execute(actorId, orderId, reason)` | Comprador dueño; orden DELIVERED; una devolucion por orden |
| ReviewReturnService (ReviewReturnUseCase) | `approve(actorId, returnId)`, `reject(actorId, returnId)` | Administrador; aprobar crea un reembolso PENDING y reingresa las unidades (Devolucion) a la primera bodega del producto |
| ProcessRefundService (ProcessRefundUseCase) | `process(actorId, returnId)`, `reject(actorId, returnId)` | Administrador; la devolucion debe estar APPROVED |
| ConsultReturnsService (ConsultReturnsUseCase) | `execute(actorId)`, `executeOne(actorId, returnId)` | comprador: las suyas; Administrador y Supervisor: todas |
| ReturnLoader | auxiliar | carga la devolucion o lanza `EntityNotFoundException` |
