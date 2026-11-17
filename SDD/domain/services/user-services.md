# Servicios de Usuarios

Paquete: `domain/services/user`. Objetivos: OBJ-01, OBJ-02.

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| RegisterBuyerService (RegisterBuyerUseCase) | `execute(document, fullName, email, Address mainAddress)` | publico; documento y correo unicos |
| RegisterSellerService (RegisterSellerUseCase) | `execute(actorId, document, fullName, email, firstWarehouseName)` | Administrador; crea la primera bodega SELLER del vendedor |
| ChangeUserStatusService (ChangeUserStatusUseCase) | `execute(actorId, userId, active)` | Administrador |
| ConsultUserService (ConsultUserUseCase) | `execute(actorId, userId)`, `executeAll(actorId)` | perfil propio; Administrador ve vendedores; Supervisor ve todos |
| UniqueUserDataValidator | auxiliar | `DuplicateDataException` si el documento o el correo ya existen |
