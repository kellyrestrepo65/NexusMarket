# Servicios de Reporte administrativo

Paquete: `domain/services/report`. Objetivos: OBJ-12.

Solo los reembolsos PROCESSED cuentan como dinero reembolsado.

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| AdministrativeReportService (AdministrativeReportUseCase) | `execute(actorId)` | Administrador o Supervisor |
