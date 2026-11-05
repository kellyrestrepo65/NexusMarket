# Servicios de Seguridad

Paquete: `domain/services/security`. Objetivos: RG-01, RG-02, RG-03.

Servicio transversal que usan todos los demas; no tiene puerto de entrada porque ningun controlador lo invoca directamente.

| Servicio | Operacion | Reglas |
|---|---|---|
| AuthorizationService | `requireActiveUser(actorId)` | el actor existe y esta ACTIVE (RG-01); si falta el id → `UnauthenticatedException` |
| | `requireRole(actorId, roles...)` | ademas tiene uno de los roles permitidos (RG-03); si no → `ForbiddenOperationException` |
