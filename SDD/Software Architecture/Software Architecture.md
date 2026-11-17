# Arquitectura de Software

## Vision general

NexusMarket es una plataforma marketplace que intermedia la relacion comercial entre compradores y vendedores. Sigue una **Arquitectura Hexagonal (Puertos y Adaptadores)** con principios de **Domain-Driven Design**.

El dominio contiene todas las reglas de negocio y nunca depende de Spring Web, JPA, MongoDB ni HTTP. Las tecnologias externas llegan al dominio a traves de adaptadores, y el dominio solo las alcanza mediante los puertos que el mismo define.

Diagramas: [`docs/arquitectura-hexagonal-nexusmarket.drawio`](../../docs/arquitectura-hexagonal-nexusmarket.drawio) (editable) y [`docs/arquitectura-hexagonal-nexusmarket.png`](../../docs/arquitectura-hexagonal-nexusmarket.png) (puertos y adaptadores, con los dos adaptadores de entrada).

---

# Principios arquitectonicos

- Diseño centrado en el dominio: el modelo se escribe antes que la persistencia.
- Inversion de dependencias: los controladores dependen de los puertos de entrada (`*UseCase`) y los servicios de los puertos de salida (`*RepositoryPort`), nunca de clases concretas de infraestructura.
- Independencia tecnologica: MySQL y MongoDB son intercambiables detras de los puertos.
- Fronteras explicitas: requests/responses, entidades de persistencia y modelos de dominio son clases distintas.
- Autorizacion en el dominio: cada servicio empieza validando el rol del actor (RG-01, RG-02, RG-03).
- Convencion de idioma: el codigo esta en ingles y los comentarios en español.

---

# Stack tecnologico

| Aspecto | Tecnologia |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.4 (Web, Validation, Data JPA, Data MongoDB) |
| Base relacional | MySQL 8.4: usuarios, bodegas, inventario, carritos, ordenes, facturas, envios, devoluciones |
| Base documental | MongoDB 7: catalogo de productos (productos con variantes variables) |
| Documentacion de la API | springdoc-openapi (Swagger UI en `/swagger-ui.html`) |
| Interfaz web | Thymeleaf (Spring MVC) renderizado en el servidor, en `/` (`src/main/resources/templates` + `static/styles.css`) |
| Pruebas | JUnit 5 + Mockito |
| Entorno local | Docker (`docker-compose.yml`) |

**Por que dos bases de datos.** Los datos transaccionales con relaciones fuertes y maquinas de estado (ordenes, inventario, devoluciones) viven en MySQL. El catalogo se lee mucho y cada producto tiene un conjunto libre de variantes (`color`, `talla`, `licencia`...), que encaja mejor en un modelo documental; por eso vive en MongoDB.

---

# Estructura de paquetes

```text
src/main/java/com/nexusmarket/
├── NexusMarketApplication.java
├── domain/
│   ├── models/
│   │   ├── user/          User, Buyer, Seller, Administrator, LogisticsOperator, Supervisor
│   │   ├── catalog/       Product, Variant
│   │   ├── inventory/     Warehouse, Inventory
│   │   ├── cart/          ShoppingCart, CartItem
│   │   ├── order/         Order, OrderItem, Invoice, Shipment, Return, Refund
│   │   └── report/        AdministrativeReport
│   ├── valueobjects/      EntityId, Address
│   ├── enums/             Role, UserStatus, CommercialStatus, ProductType, ProductStatus,
│   │                      WarehouseType, OrderStatus, ShipmentStatus, ReturnStatus, RefundStatus
│   ├── exceptions/        jerarquia de DomainException
│   ├── ports/
│   │   ├── in/            *UseCase: un puerto de entrada por caso de uso
│   │   └── out/           *RepositoryPort: lo que el dominio necesita del exterior
│   └── services/          un servicio por caso de uso (implementa su *UseCase), agrupados por subdominio
├── application/
│   └── adapters/
│       ├── in/web/
│       │   ├── controllers/   controladores Thymeleaf (*WebController, dependen de los *UseCase)
│       │   ├── handlers/      WebControllerAdvice (modelo comun y errores del dominio)
│       │   └── support/       WebSession (actor en la sesion), WebMenu (pestanas por rol)
│       ├── in/rest/
│       │   ├── controllers/   controladores REST (dependen de los *UseCase)
│       │   ├── requests/      Requests (cuerpos de peticion)
│       │   ├── responses/     Responses (cuerpos de respuesta)
│       │   └── handlers/      GlobalExceptionHandler
│       └── out/persistence/
│           ├── mysql/     entities/, repositories/ (Spring Data JPA), adapters/
│           └── mongodb/   documents/, repositories/, adapters/
└── infrastructure/
    ├── config/            StartupInitializer (usuarios internos), OpenApiConfig
    └── security/          CurrentUser (lectura de la cabecera X-User-Id)

src/main/resources/
├── application.yml
├── templates/             vistas Thymeleaf (fragments/layout.html y una pagina por pantalla)
└── static/                styles.css
```

---

# Responsabilidades por capa

## Dominio

- **Modelos**: entidades que protegen sus propias invariantes (por ejemplo, `Inventory` nunca queda negativo y `Order` solo avanza un estado a la vez).
- **Objetos de valor y enums**: conceptos inmutables sin identidad (`Address`, `EntityId`, estados).
- **Puertos de entrada (`ports/in`)**: el contrato de cada caso de uso que invoca la capa de aplicacion.
- **Servicios**: implementan un caso de uso: autorizan al actor, cargan modelos por los puertos de salida, invocan el comportamiento del modelo y persisten.
- **Puertos de salida (`ports/out`)**: lo que el dominio necesita del mundo exterior.
- **Excepciones**: fallos de negocio, traducidos a HTTP solo en el borde.

## Adaptadores de entrada

Hay dos adaptadores de entrada y ambos usan los mismos `*UseCase`, sin duplicar logica de negocio.

### Web (`application/adapters/in/web`)

- Controladores Spring MVC que devuelven vistas Thymeleaf y procesan formularios (patron POST-redirect-GET con mensajes flash).
- El actor se guarda en la `HttpSession` tras el ingreso (`WebSession`); la sesion viaja solo por cookie.
- `WebMenu` arma las pestanas segun el rol; la autorizacion real sigue en el dominio.
- `WebControllerAdvice` muestra los errores del dominio como mensaje en la pagina o en `error-page`.

### REST (`application/adapters/in/rest`)

- Reciben HTTP/JSON y validan la forma con Bean Validation (`@NotBlank`, `@Positive`...).
- Leen al usuario actor de la cabecera `X-User-Id` (`CurrentUser`).
- Convierten las peticiones en parametros del dominio y los modelos en `Responses`.
- `GlobalExceptionHandler` traduce las excepciones del dominio a codigos HTTP.

## Adaptadores de salida (`application/adapters/out/persistence`)

- Implementan cada `*RepositoryPort`.
- Convierten modelos de dominio a entidades JPA o documentos Mongo y viceversa.

## Infraestructura

- `StartupInitializer` crea un Administrador, un Operador Logistico y un Supervisor la primera vez que arranca la plataforma.
- `OpenApiConfig` publica el contrato de la API.
- `CurrentUser` identifica al actor de cada peticion.

---

# Decision: conversion de datos sin clases mapper

No se crearon clases `*Mapper` separadas. La conversion vive junto a la clase que conoce ambos lados:

- Entidades JPA y documentos Mongo: metodo estatico `from(modelo)` y metodo `toDomain()`. Al reconstruir se usan los constructores y metodos de estado del dominio, asi un objeto cargado pasa las mismas validaciones que uno nuevo.
- `Responses`: cada record tiene un `from(modelo)`.
- `Requests`: los records que construyen objetos de valor tienen `toDomain()` (por ejemplo `AddressRequest`).

Se eligio asi porque cada conversion es corta y uno a uno; separarla en otra clase solo agregaria indireccion. Si una conversion crece o se reutiliza entre adaptadores, se extrae a un mapper.

---

# Flujo de una peticion

```text
Peticion HTTP
  → Controller (valida el cuerpo, lee X-User-Id)
    → XxxUseCase (puerto de entrada)
      → XxxService (AuthorizationService.requireRole → carga por puertos → comportamiento del modelo → guarda por puertos)
        → RepositoryPort (puerto de salida)
          → Adaptador MySQL/Mongo → base de datos
  ← Response   |   DomainException → GlobalExceptionHandler → error JSON
```

---

# Manejo de errores

| Excepcion | HTTP |
|---|---|
| `InvalidArgumentException`, validacion de Bean Validation | 400 |
| `UnauthenticatedException` (falta `X-User-Id`) | 401 |
| `ForbiddenOperationException` | 403 |
| `EntityNotFoundException` | 404 |
| `DuplicateDataException` | 409 |
| `BusinessRuleException`, `InvalidStateTransitionException`, `InsufficientInventoryException`, `OrderFinalizedException` | 422 |

Cuerpo de error: `{"status": 422, "error": "UNPROCESSABLE_ENTITY", "message": "..."}`.

---

# Alcance de la autenticacion

La especificacion deja fuera del alcance la autenticacion tecnica (RG-01 solo exige que toda operacion la ejecute un usuario identificado). El actor se identifica con la cabecera `X-User-Id`; luego el dominio verifica que el usuario exista, este activo y tenga un rol permitido. La interfaz web pide ese id al ingresar y lo envia en cada peticion.

---

# Estrategia de pruebas

- **Pruebas unitarias** (`src/test`): servicios con puertos simulados (Mockito), verificando autorizacion, reglas de negocio y lo que se persiste. Se ejecutan con `mvn test`.
- **Pruebas de integracion** (`src/test/.../integration`): `MarketplaceFlowIntegrationTest` levanta el contexto completo y recorre la API con MockMvc contra MySQL y MongoDB reales (bases `nexusmarket_test`). Se omiten si las bases no estan disponibles.
- **Prueba de humo de punta a punta**: `scripts/smoke_test.py` recorre el flujo completo compra → devolucion → reporte contra la API en ejecucion y comprueba los rechazos por rol.
