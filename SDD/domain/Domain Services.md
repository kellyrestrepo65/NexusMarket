# Servicios del Dominio

## Introduccion

Cada caso de uso es un servicio en `domain/services/<subdominio>` que implementa su puerto de entrada `domain/ports/in/<Nombre>UseCase`. Los controladores dependen solo del puerto de entrada. Los servicios reciben modelos de dominio, objetos de valor (`EntityId`, `Address`, enums) y el id del usuario actor; nunca reciben requests ni entidades de persistencia.

Todos los servicios siguen los mismos pasos:

1. `AuthorizationService.requireRole(actorId, roles permitidos...)`: el actor existe, esta ACTIVE y tiene un rol permitido (RG-01/02/03).
2. Cargar los modelos por los puertos de salida (si no existe → `EntityNotFoundException`).
3. Aplicar la regla llamando al comportamiento del modelo.
4. Persistir por los puertos de salida y devolver el modelo.

Los auxiliares (`OrderLoader`, `ReturnLoader`, `InventoryAccessValidator`, `UniqueUserDataValidator`) y `AuthorizationService` no tienen puerto de entrada porque solo los usan otros servicios.

---

# Matriz de responsabilidades

| Subdominio | Comprador | Vendedor | Operador Logistico | Administrador | Supervisor |
|---|---|---|---|---|---|
| Registro de vendedores | | | | ✔ | |
| Usuarios (estado, listado) | perfil propio | perfil propio | perfil propio | ✔ | consulta |
| Bodegas | | propias (consulta) | consulta | ✔ | consulta |
| Productos | consulta | ✔ | | | |
| Inventario | | ✔ productos propios | ✔ | | consulta |
| Carrito, checkout, pago | ✔ | | | | |
| Ordenes | propias | ordenes con sus productos | ✔ despacho / entrega | | consulta |
| Facturas, envios | propios | | envios | ✔ | consulta |
| Devoluciones | solicitud | | | ✔ revision | consulta |
| Reembolsos | | | | ✔ | consulta |
| Reporte administrativo | | | | ✔ | ✔ |

---

# Servicios por subdominio

- [Seguridad](services/security-services.md)
- [Usuarios](services/user-services.md)
- [Bodegas](services/warehouse-services.md)
- [Catalogo](services/catalog-services.md)
- [Inventario](services/inventory-services.md)
- [Carrito](services/cart-services.md)
- [Ordenes](services/order-services.md)
- [Facturacion y logistica](services/billing-logistics-services.md)
- [Devoluciones y reembolsos](services/returns-services.md)
- [Reporte administrativo](services/report-services.md)
