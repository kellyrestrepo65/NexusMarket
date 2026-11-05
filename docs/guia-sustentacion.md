# Guia de sustentacion - NexusMarket

## 1. Preparacion (antes de entrar)

```bash
docker compose up -d          # MySQL 3306 y MongoDB 27017
mvn spring-boot:run           # http://localhost:8080
```

Usuarios internos: `100000` Administrador, `100001` Operador Logistico, `100002` Supervisor.
Swagger: http://localhost:8080/swagger-ui.html (boton *Authorize* no aplica: el actor va en la cabecera `X-User-Id`).

## 2. Guion de la demostracion de la API (unos 10 minutos)

| # | Paso | Endpoint | Actor | Que demuestra |
|---|------|----------|-------|---------------|
| 1 | Registrar vendedor con su primera bodega | `POST /api/users/sellers` | 100000 | El vendedor no se autorregistra (Dominio 3) |
| 2 | Autorregistro de comprador | `POST /api/users/buyers` | ninguno | Unico endpoint publico |
| 3 | Intentar registrar vendedor como comprador | `POST /api/users/sellers` | comprador | RG-03 → **403** |
| 4 | Crear producto con variantes | `POST /api/products` | vendedor | Catalogo en **MongoDB** |
| 5 | Ingresar inventario | `POST /api/inventories/receipts` | vendedor | Inventario en **MySQL**, ligado a producto y bodega |
| 6 | Agregar al carrito y confirmar | `POST /api/cart/items`, `POST /api/orders` | comprador | Carrito → Pendiente de pago, reserva de inventario |
| 7 | Pedir mas de lo disponible | `POST /api/orders` | comprador | Sin existencias negativas → **422** |
| 8 | Pagar y ver factura | `POST /api/orders/{id}/payment`, `GET .../invoice` | comprador | Pagado + facturacion |
| 9 | Despachar y entregar | `POST .../dispatch`, `POST .../delivery` | 100001 | Logistica; el vendedor no puede despachar → 403 |
| 10 | Devolucion y reembolso | `POST /api/returns`, `.../approval`, `.../refund/processing` | comprador / 100000 | OBJ-11 |
| 11 | Reporte administrativo | `GET /api/reports/administrative` | 100002 | OBJ-12; un comprador recibe 403 |
| 12 | Peticion sin `X-User-Id` | `GET /api/orders` | ninguno | RG-01 → **401** |

Atajo: `python scripts/smoke_test.py 100000 100001 100002` ejecuta todo el guion (33 chequeos).

## 3. Decisiones que hay que saber defender

- **Arquitectura hexagonal.** El dominio no depende de Spring, JPA ni Mongo. Los controllers dependen de `*UseCase` (puertos de entrada) y los servicios de `*RepositoryPort` (puertos de salida). Prueba de ello: la API REST y la web Thymeleaf usan los mismos casos de uso sin duplicar logica.
- **Un servicio por caso de uso.** 30 `UseCase`, cada uno con su servicio: responsabilidad unica y pruebas aisladas.
- **Dos bases de datos.** MySQL para lo transaccional y con relaciones fuertes (usuarios, pedidos, inventario, facturas, envios, devoluciones). MongoDB para el catalogo, porque las variantes de cada producto son libres (color, talla, licencia...).
- **Autorizacion en el dominio.** Cada servicio empieza con `AuthorizationService.requireRole`; asi RG-01/02/03 se cumplen sin importar el adaptador de entrada.
- **Autenticacion por `X-User-Id`.** La seccion 3.2 del enunciado deja fuera la autenticacion tecnica; el dominio igual valida que el usuario exista, este activo y tenga el rol.
- **Invariantes en los modelos.** `Inventory` nunca queda negativo ni reserva lo danado; `Order` solo avanza un estado a la vez y no se modifica finalizado.
- **Errores.** `GlobalExceptionHandler` traduce excepciones del dominio: 400 validacion, 401 sin actor, 403 rol, 404 no existe, 409 duplicado, 422 regla de negocio.
- **Sin clases mapper.** Cada entidad/documento tiene `from()` y `toDomain()`; la conversion es corta y uno a uno.

## 4. Pruebas

- `mvn test`: 30 pruebas unitarias de servicios (Mockito) + 2 pruebas de integracion (`MarketplaceFlowIntegrationTest`) que recorren la API contra MySQL y MongoDB reales en bases `nexusmarket_test`. Si las bases no estan arriba, las de integracion se omiten.

## 5. Preguntas probables

- *¿Que pasa si cambio MySQL por PostgreSQL?* Solo se cambia el adaptador de salida; el dominio no se toca.
- *¿Donde se valida que el inventario no sea negativo?* En el modelo `Inventory`, no en el controller.
- *¿Como sabe el sistema quien hace la peticion?* `CurrentUser` lee `X-User-Id` y el dominio verifica rol y estado.
- *¿Por que el producto digital no pasa por despacho?* Se entrega al pagar; no requiere inventario ni envio.
- *Limitacion conocida:* los movimientos de inventario se aplican sobre las cantidades, pero aun no se guarda un historial de cada movimiento.
