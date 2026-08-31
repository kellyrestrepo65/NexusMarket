# Modelo de Dominio

## Introduccion

Este documento explica las clases principales de NexusMarket, un marketplace de e-commerce, y por que estan hechas asi.

El modelo se divide en cinco temas:

* **Usuarios**, cada persona que usa la plataforma, segun su rol.
* **Catalogo**, los productos que un vendedor publica y sus variantes.
* **Inventario**, donde esta el stock fisicamente y cuanto hay disponible o reservado.
* **Carrito**, lo que un comprador va juntando antes de confirmar la compra.
* **Orden**, una compra ya confirmada y todo lo que viene despues: factura, envio, devoluciones y reembolsos.

Todas las entidades tienen un id de tipo `EntityId`, en vez de un `UUID` de Java o un numero suelto (ver `Domain Value Objects.md`).

---

## Jerarquia de clases

```text
User (abstracta)
├── Buyer
├── Seller
├── LogisticsOperator
├── Administrator
└── Supervisor

Product
Variant

Warehouse
Inventory

ShoppingCart
CartItem

Order
OrderItem
Invoice
Shipment
Return
Refund
```

---

## Como se relacionan las clases

```text
User (abstracta)
   │
   ├── Buyer
   │      └── mainAddress, additionalAddresses : Address
   │
   ├── Seller
   │      ├── warehouses : List<Warehouse>
   │      └── products   : List<Product>
   │
   ├── LogisticsOperator
   ├── Administrator
   └── Supervisor

Seller
   ├── es dueño de ──────> Warehouse (objetos completos, no solo el id)
   └── publica ──────────> Product (objetos completos, no solo el id)

Product
   ├── tiene ────────────> Variant (objetos completos)
   ├── sellerId (por id) ─> Seller
   └── referenciado por id desde Inventory.productId, CartItem.productId, OrderItem.productId

Warehouse
   ├── ownerId (por id, puede ser null) ─> Seller
   └── referenciado por id desde Inventory.warehouseId

Inventory
   ├── productId (por id) ──> Product
   └── warehouseId (por id) ─> Warehouse

ShoppingCart
   ├── buyerId (por id) ────> Buyer
   └── items : List<CartItem>

CartItem
   └── productId (por id) ──> Product

Order
   ├── buyerId (por id) ────> Buyer
   └── items : List<OrderItem>

OrderItem
   └── productId (por id) ──> Product

Invoice
   └── orderId (por id) ────> Order

Shipment
   ├── orderId (por id) ────────────> Order
   └── logisticsOperatorId (por id) ─> LogisticsOperator

Return
   ├── orderId (por id) ────> Order
   └── genera ──────────────> Refund (lo crea directamente, no por id)

Refund
   └── returnId (por id) ───> Return
```

---

# Las clases

---

## User (abstracta)

Es la clase base de todos los usuarios del sistema. Junta lo que cualquier persona en la plataforma necesita: identidad, contacto, rol y estado de cuenta. Es abstracta porque un "User" solo, sin rol, no existe en la practica — siempre es un `Buyer`, `Seller`, `LogisticsOperator`, `Administrator` o `Supervisor`.

### Atributos

| Atributo | Tipo       | Por que existe                                                             |
| --------- | ---------- | ---------------------------------------------------------------------------- |
| id        | EntityId   | Identifica al usuario.                                                    |
| fullName  | String     | Nombre completo.                                                       |
| email     | String     | Correo, se valida con un formato basico.           |
| role      | Role       | El rol del usuario. Se fija en el constructor de cada subclase y no tiene setter, para que nadie pueda cambiar de rol despues de creado. |
| status    | UserStatus | Estado de la cuenta (`ACTIVE`, `BLOCKED`, `INACTIVE`).       |

### Metodos

* `changeFullName(String newFullName)` — cambia el nombre, no deja que quede vacio.
* `changeEmail(String newEmail)` — cambia el email, validando el formato con una expresion regular.
* `block()` — pone `status` en `BLOCKED`.
* `activate()` — pone `status` en `ACTIVE`.
* `isActive()` — dice si `status == ACTIVE`.
* `canOperateOn(User other)` — ver la seccion de abajo.

### canOperateOn

Cada rol solo deberia poder operar dentro de su propio alcance. Por defecto, en `User`, esto significa que un usuario solo puede operar sobre si mismo:

```text
canOperateOn(other) := this.id.equals(other.id)
```

Dos subclases cambian este comportamiento:

* `Administrator.canOperateOn(other)` devuelve `true` si `other` es un `Seller`, y si no, usa el comportamiento por defecto (solo si mismo). Esto es porque un Administrator es quien da de alta y gestiona vendedores.
* `Seller.canOperateOn(other)` llama a `super.canOperateOn(other)` sin agregar nada — un vendedor no tiene ninguna autorizacion especial sobre otros usuarios.

`Supervisor` no sobrescribe este metodo, se queda con el comportamiento por defecto (solo opera sobre si mismo).

Este metodo esta en el modelo, no en un servicio aparte, porque solo necesita datos que ya estan en `User` — el rol de quien llama y el tipo del usuario objetivo. No hace falta ningun dato externo.

---

## Buyer

Un comprador: puede ver el catalogo, armar un carrito y hacer pedidos. Hereda de `User`.

### Atributos

| Atributo            | Tipo            | Por que existe                                                              |
| -------------------- | --------------- | ------------------------------------------------------------------------- |
| mainAddress           | Address         | Direccion principal, obligatoria desde la construccion.               |
| additionalAddresses   | List\<Address\> | Otras direcciones a las que puede enviar. Empieza vacia.             |
| commercialStatus      | CommercialStatus | Situacion comercial (`ENABLED`, `RESTRICTED`).               |

### Metodos

* `addAdditionalAddress(Address address)` — agrega una direccion a la lista.
* `changeMainAddress(Address newAddress)` — reemplaza la direccion principal.
* `restrict()` — pone `commercialStatus` en `RESTRICTED`.
* `enable()` — pone `commercialStatus` en `ENABLED`.
* `canPurchase()` — devuelve `true` solo si `isActive() && commercialStatus == ENABLED`. Se necesitan las dos condiciones a la vez para poder comprar: el estado general de la cuenta (heredado de `User`) y el estado comercial propio del comprador.

### Relaciones

* `ShoppingCart` y `Order` guardan `buyerId` para referenciar al comprador.

---

## Seller

Un vendedor: publica productos y administra las bodegas donde guarda su stock. Hereda de `User`.

Un `Seller` siempre tiene al menos una bodega desde que se crea — la bodega inicial es un parametro del constructor, no algo que se agrega despues, porque un vendedor necesita un lugar donde tener su inventario desde el primer momento. No hay un metodo "register" separado aqui porque un vendedor no se registra solo, lo da de alta un `Administrator`.

### Atributos

| Atributo  | Tipo              | Por que existe                                                                 |
| ---------- | ----------------- | ---------------------------------------------------------------------------- |
| warehouses | List\<Warehouse\> | Las bodegas de este vendedor. Empieza con la bodega inicial del constructor. |
| products   | List\<Product\>   | Los productos publicados por este vendedor. Empieza vacia.                        |

### Por que Seller guarda los objetos completos y no solo el id

`Seller` guarda sus bodegas y productos como objetos completos (`List<Warehouse>`, `List<Product>`), no como una lista de ids. La mayoria de las otras clases del modelo usan solo el id para referenciarse (por ejemplo `Product.sellerId`, `Inventory.warehouseId`), pero aca se uso el objeto completo porque cuando se trabaja con un vendedor casi siempre se necesita ver tambien lo que tiene.

### Metodos

* El constructor `Seller(EntityId id, String fullName, String email, Warehouse initialWarehouse)` valida que `initialWarehouse.getType() == WarehouseType.SELLER`.
* `addWarehouse(Warehouse warehouse)` — agrega una bodega, validando que sea de tipo `SELLER` (un vendedor no puede agregarse a si mismo una bodega de tipo `MARKETPLACE`).
* `publishProduct(Product product)` — agrega un producto, validando que `product.getSellerId().equals(this.getId())`, es decir, un vendedor solo puede publicar productos que ya lo tengan a el como dueño.
* `canOperateOn(User other)` — sobrescribe el metodo heredado pero solo delega en `super`, sin agregar nada.

---

## LogisticsOperator

El operador logistico se encarga de la operacion fisica de bodegas y despachos. Hereda de `User` y no tiene atributos propios, solo el rol lo distingue de los demas usuarios.

### Relaciones

* `Shipment` guarda `logisticsOperatorId` una vez que se le asigna un envio.

---

## Administrator

El administrador maneja vendedores y bodegas. Hereda de `User`.

### Metodos

* `canOperateOn(User other)` — sobrescrito: devuelve `true` cuando `other instanceof Seller`, y si no, usa el comportamiento por defecto (solo si mismo). Es el unico que puede registrar un vendedor nuevo (ver `Seller`, no tiene auto-registro).

---

## Supervisor

El supervisor solo consulta reportes y el estado general del sistema, no modifica otros usuarios. Hereda de `User` y no sobrescribe ningun metodo — se queda con el `canOperateOn` por defecto, que solo le permite operar sobre si mismo.

---

## Product

Un producto publicado por un vendedor en el catalogo.

Un producto puede ser fisico o digital, pero es una sola clase `Product` con un campo `type` (`ProductType`) para distinguirlos, no dos clases separadas. Todos los demas atributos (nombre, precio, variantes, estado) son iguales para los dos tipos, y lo unico que cambia es si necesita envio fisico, que se resuelve con un metodo que devuelve `true`/`false`: `requiresPhysicalShipping()`.

### Atributos

| Atributo | Tipo         | Por que existe                                                                 |
| --------- | ------------ | ---------------------------------------------------------------------------- |
| id        | EntityId     | Identifica al producto.                                 |
| sellerId  | EntityId     | El vendedor dueño. No cambia — un producto no puede cambiar de dueño.  |
| name      | String       | Nombre del producto. No puede quedar vacio.                                               |
| price     | BigDecimal   | Precio. No puede ser negativo.                                               |
| type      | ProductType  | `PHYSICAL` o `DIGITAL`. Se fija en la construccion.                   |
| status    | ProductStatus | `PUBLISHED`, `SUSPENDED`, o `DISCONTINUED`.                                  |
| variants  | List\<Variant\> | Variantes de este producto (por ejemplo color, talla). Empieza vacia.            |

### Metodos

* `changeName(String newName)` / `changePrice(BigDecimal newPrice)` — cambian nombre y precio; rechazan nombre vacio y precio negativo.
* `addVariant(Variant variant)` — agrega una variante; rechaza `null`.
* `suspend()` — pasa a `SUSPENDED`; lanza `InvalidStateTransitionException` si el producto ya esta `DISCONTINUED`.
* `publish()` — pasa a `PUBLISHED`; lanza `InvalidStateTransitionException` si el producto ya esta `DISCONTINUED`.
* `discontinue()` — pasa a `DISCONTINUED` sin ninguna restriccion (siempre se puede descontinuar, y es justamente lo que hace que las otras dos transiciones queden bloqueadas despues).
* `isAvailableForPurchase()` — `true` cuando `status == PUBLISHED`.
* `requiresPhysicalShipping()` — `true` cuando `type == PHYSICAL`.

### Transiciones de estado

```text
PUBLISHED ──suspend()──> SUSPENDED
SUSPENDED ──publish()──> PUBLISHED
PUBLISHED ──discontinue()──> DISCONTINUED
SUSPENDED ──discontinue()──> DISCONTINUED
DISCONTINUED ──suspend()/publish()──> InvalidStateTransitionException
```

`DISCONTINUED` es un estado final para `suspend`/`publish`: una vez ahi, un producto ya no puede volver a suspenderse ni a publicarse.

---

## Variant

Una variante de un producto, por ejemplo color: rojo o talla: M. Tiene su propio id pero, una vez creada, ninguno de sus datos cambia.

### Atributos

| Atributo | Tipo     | Por que existe                                    |
| --------- | -------- | ----------------------------------------------- |
| id        | EntityId | Identifica la variante.    |
| attribute | String   | El nombre del atributo (por ejemplo "color"). No puede quedar vacio. |
| value     | String   | El valor (por ejemplo "rojo"). No puede quedar vacio. |

### Relaciones

* Vive dentro de `Product.variants`, no tiene existencia propia fuera de un producto.

---

## Warehouse

Una bodega. Puede ser del marketplace (`ownerId` es `null`) o de un vendedor especifico (`ownerId` es obligatorio). Es una sola clase con el campo `type` para distinguir las dos, no dos clases separadas, porque lo unico que cambia entre una y otra es quien es el dueño.

### Atributos

| Atributo | Tipo          | Por que existe                                                                            |
| --------- | ------------- | ---------------------------------------------------------------------------------------- |
| id        | EntityId      | Identifica la bodega.                                          |
| name      | String        | Nombre de la bodega. No puede quedar vacio.                                                         |
| type      | WarehouseType | `MARKETPLACE` o `SELLER`. Se fija en la construccion.                            |
| ownerId   | EntityId      | Id del vendedor dueño. `null` si `type == MARKETPLACE`; obligatorio si `type == SELLER`.  |

### Construccion

El constructor de `Warehouse` es privado; solo se puede crear una bodega con dos metodos estaticos, que son los que garantizan que `ownerId` siempre este bien puesto segun el tipo:

* `marketplaceWarehouse(EntityId id, String name)` — crea una bodega `MARKETPLACE` con `ownerId = null`.
* `sellerWarehouse(EntityId id, String name, EntityId ownerSellerId)` — crea una bodega `SELLER`; lanza `InvalidArgumentException` si `ownerSellerId` es `null`.

### Metodos

* `changeName(String newName)` — cambia el nombre; rechaza nombre vacio.

---

## Inventory

El inventario de un producto en una bodega puntual: cuanto hay disponible y cuanto esta reservado. El stock nunca puede quedar negativo, por eso las cantidades solo cambian a traves de los metodos de movimiento (`receiveStock`, `reserve`, `releaseReservation`, `confirmDispatch`, `adjust`), cada uno con su propia validacion.

### Atributos

| Atributo          | Tipo     | Por que existe                                                        |
| ------------------ | -------- | -------------------------------------------------------------------- |
| id                  | EntityId | Identifica el registro de inventario.               |
| productId           | EntityId | El producto que este registro rastrea.            |
| warehouseId         | EntityId | La bodega a la que pertenece este registro.      |
| availableQuantity   | int      | Unidades fisicamente presentes en la bodega. Empieza en 0.       |
| reservedQuantity    | int      | Unidades reservadas (por ejemplo para pedidos pendientes). Empieza en 0.    |

### Metodos

* `receiveStock(int quantity)` — aumenta `availableQuantity` (llega mercancia a la bodega).
* `reserve(int quantity)` — aumenta `reservedQuantity`; lanza `InsufficientInventoryException` si `getAvailableFreeQuantity() < quantity`.
* `releaseReservation(int quantity)` — baja `reservedQuantity` (por ejemplo un pedido cancelado libera su reserva de stock).
* `confirmDispatch(int quantity)` — baja tanto `availableQuantity` como `reservedQuantity` (el stock sale fisicamente de la bodega).
* `registerReturn(int quantity)` — aumenta `availableQuantity` (la mercancia devuelta vuelve al stock disponible).
* `adjust(int delta)` — aplica una correccion manual con signo a `availableQuantity`; nunca deja que el resultado sea negativo ni menor a `reservedQuantity`.
* `getAvailableFreeQuantity()` — se calcula como `availableQuantity - reservedQuantity`, lo que realmente se puede volver a reservar.

### Regla de reserva

```text
reserve(quantity) solo es valido cuando:
    availableQuantity - reservedQuantity >= quantity
si no:
    InsufficientInventoryException
```

---

## ShoppingCart

Lo que un comprador va juntando antes de confirmar la compra. Un carrito se convierte en un `Order` cuando el comprador confirma la compra; el carrito en si no es un documento de negocio final.

### Atributos

| Atributo | Tipo              | Por que existe                                             |
| --------- | ----------------- | -------------------------------------------------------- |
| id        | EntityId          | Identifica el carrito.                |
| buyerId   | EntityId          | El comprador dueño.               |
| items     | List\<CartItem\>  | Los productos que hay actualmente en el carrito. Empieza vacia.      |

### Metodos

* `addProduct(EntityId productId, int quantity)` — aumenta la cantidad de un `CartItem` que ya existe para ese producto, o crea uno nuevo si no existe (busca con un metodo privado `findItem()`).
* `changeQuantity(EntityId productId, int newQuantity)` — pone directamente la cantidad de un item que ya esta en el carrito.
* `removeProduct(EntityId productId)` — saca por completo el item de ese producto.
* `clear()` — vacia el carrito.
* `isEmpty()` — dice si el carrito no tiene items.

---

## CartItem

Un producto dentro del carrito, con su cantidad. Siempre se crea, modifica y elimina a traves de `ShoppingCart`, nunca solo, por eso el constructor es package-private (no se puede usar desde fuera del paquete `cart`).

### Atributos

| Atributo  | Tipo     | Por que existe                                             |
| ---------- | -------- | ---------------------------------------------------------- |
| productId  | EntityId | El producto referenciado.         |
| quantity   | int      | La cantidad de ese producto en el carrito.                     |

### Metodos

* `increment(int units)` — package-private; suma a la cantidad actual.
* `changeQuantity(int newQuantity)` — package-private; reemplaza la cantidad actual.

### Relaciones

* Dos `CartItem` son iguales si tienen el mismo `productId` (un carrito no puede tener dos lineas separadas para el mismo producto).

---

## Order

Una compra ya confirmada por un comprador. El estado de una orden sigue un ciclo fijo y en una sola direccion (ver `OrderStatus` en `Domain Value Objects.md`), y una vez que llega a su estado final se vuelve inmutable — esto lo aplica la propia clase `Order`, no solo el servicio que la llama.

### Atributos

| Atributo | Tipo              | Por que existe                                                |
| --------- | ----------------- | ------------------------------------------------------------ |
| id        | EntityId          | Identifica la orden.                   |
| buyerId   | EntityId          | El comprador que hizo la orden.      |
| items     | List\<OrderItem\> | Los productos de la orden.                                      |
| status    | OrderStatus       | El estado actual. Empieza en `CART`.                   |

### Metodos

* `addItem(OrderItem item)` / `removeItem(EntityId productId)` — ambos llaman primero a un metodo privado `validateNotFinalized()`, que lanza `OrderFinalizedException` si `status.isFinal()`.
* `advanceTo(OrderStatus nextStatus)` — le pregunta a `status.canAdvanceTo(nextStatus)` si el cambio es valido; lanza `InvalidStateTransitionException` si no lo es.
* `requiresShipment()` — dice si la orden necesita un `Shipment` (tiene al menos un item).
* `calculateTotal()` — suma el subtotal de cada `OrderItem` de la orden.
* `isFinalized()` — `true` cuando `status.isFinal()`.

### Order y sus clases relacionadas

`Invoice`, `Shipment`, y `Return` son clases independientes que cada una guarda el id de su `Order` (`orderId`), en vez de ser campos dentro de `Order`. Cada una tiene su propio ciclo de vida: un envio avanza por sus propios estados despues de que la orden ya esta `PAID`/`DISPATCHED`, y una devolucion se puede pedir mucho despues de que la orden este `DELIVERED`.

---

## OrderItem

Un producto dentro de una orden ya confirmada. A diferencia de `CartItem`, aqui el precio unitario queda congelado al momento de la compra, para que si el precio del producto cambia despues no afecte ordenes que ya se hicieron.

### Atributos

| Atributo | Tipo       | Por que existe                                                     |
| --------- | ---------- | ------------------------------------------------------------------ |
| productId | EntityId   | El producto comprado.                   |
| quantity  | int        | La cantidad comprada.                                     |
| unitPrice | BigDecimal | El precio por unidad en el momento de la compra.                 |

### Metodos

* `getSubtotal()` — se calcula como `unitPrice * quantity`.

---

## Invoice

La factura de una orden. Cada orden genera exactamente una factura cuando se confirma el pago, con el total ya calculado y cerrado (no se vuelve a recalcular despues).

### Atributos

| Atributo  | Tipo          | Por que existe                                     |
| ---------- | ------------- | -------------------------------------------------- |
| id         | EntityId      | Identifica la factura.       |
| orderId    | EntityId      | La orden facturada.       |
| total      | BigDecimal    | El monto total. Debe ser `>= 0`.       |
| issueDate  | LocalDateTime | Cuando se emitio la factura.   |

No tiene metodos que la cambien despues de creada — una vez emitida, una factura no se modifica.

---

## Shipment

El envio de una orden. Solo existe si la orden tiene al menos un producto fisico (ver `Product.requiresPhysicalShipping()`); una orden hecha solo de productos digitales nunca tiene un `Shipment` asociado.

### Atributos

| Atributo            | Tipo            | Por que existe                                                        |
| -------------------- | --------------- | --------------------------------------------------------------------- |
| id                    | EntityId        | Identifica el envio.                        |
| orderId               | EntityId        | La orden enviada.                          |
| logisticsOperatorId   | EntityId        | El operador logistico asignado. `null` hasta que se le asigne uno. |
| status                | ShipmentStatus  | `IN_PREPARATION`, `IN_TRANSIT`, o `DELIVERED`. Empieza en `IN_PREPARATION`. |

### Metodos

* `assignOperator(EntityId logisticsOperatorId)` — asigna el operador.
* `markInTransit()` — pasa a `IN_TRANSIT`; solo valido desde `IN_PREPARATION`.
* `markDelivered()` — pasa a `DELIVERED`; solo valido desde `IN_TRANSIT`.

### Transiciones de estado

```text
IN_PREPARATION ──markInTransit()──> IN_TRANSIT ──markDelivered()──> DELIVERED
```

Cualquier otro intento de cambio de estado se rechaza.

---

## Return

Una devolucion solicitada por un comprador sobre una orden ya entregada (o parte de ella), y decide si esa devolucion se aprueba o se rechaza.

`Return` es la unica clase del dominio que crea otra clase por dentro (`Refund`) como parte de uno de sus propios metodos (`approve`), en vez de que quien la llama construya el `Refund` y se lo pase por parametro.

### Atributos

| Atributo | Tipo          | Por que existe                                                     |
| --------- | ------------- | ------------------------------------------------------------------- |
| id        | EntityId      | Identifica la devolucion. |
| orderId   | EntityId      | La orden que se devuelve.                   |
| reason    | String        | El motivo de la devolucion, dado por el comprador.             |
| status    | ReturnStatus  | `REQUESTED`, `APPROVED`, o `REJECTED`. Empieza en `REQUESTED`.         |
| refund    | Refund        | El reembolso generado una vez que se aprueba la devolucion. `null` hasta entonces. |

### Metodos

* `approve(EntityId refundId, BigDecimal refundAmount)` — solo valido desde `REQUESTED`; crea internamente `this.refund = new Refund(refundId, this.id, refundAmount)` y pasa `status` a `APPROVED`.
* `reject()` — solo valido desde `REQUESTED`; pasa `status` a `REJECTED` sin crear ningun reembolso.
* `getRefund()` — devuelve el `Refund`, o `null` si todavia no se ha aprobado la devolucion.

---

## Refund

El reembolso que se genera cuando una devolucion se aprueba (ver `Return.approve()`).

### Atributos

| Atributo | Tipo          | Por que existe                                            |
| --------- | ------------- | ----------------------------------------------------------- |
| id        | EntityId      | Identifica el reembolso.                |
| returnId  | EntityId      | La devolucion de origen.           |
| amount    | BigDecimal    | El monto del reembolso. Debe ser `> 0`.                               |
| status    | RefundStatus  | `PENDING`, `PROCESSED`, o `REJECTED`. Empieza en `PENDING`. |

### Metodos

* `process()` — pasa de `PENDING` a `PROCESSED`.
* `reject()` — pasa de `PENDING` a `REJECTED`.

Solo se crea desde `Return.approve(...)`, nunca por su cuenta.

---

## El ciclo completo, del carrito a una posible devolucion

```text
ShoppingCart
      │
      │ confirmOrder(...)
      ▼
   Order (status = CART)
      │
      │ advanceTo(...)
      ▼
PENDING_PAYMENT ──> PAID ──> DISPATCHED ──> DELIVERED
      │                          │              │
      │                          ▼              │
      │                      Shipment           │
      │                                          ▼
      │                                       Return
      │                                          │
      │                                          ▼
      │                                       Refund
      ▼
   Invoice (se genera cuando la orden se confirma)
```

`DELIVERED` es el estado final de `Order`; una vez ahi, `addItem`/`removeItem` se rechazan siempre con `OrderFinalizedException`.

---

# Un resumen de por que quedaron asi algunas clases

## Jerarquia de usuarios

* Todos los roles concretos (`Buyer`, `Seller`, `LogisticsOperator`, `Administrator`, `Supervisor`) heredan de la clase abstracta `User`, porque todos comparten los mismos datos basicos (nombre, correo, estado de cuenta) y solo cambia el rol.
* `role` se fija en el constructor y no tiene setter — un `User` nunca cambia de rol despues de creado.
* `canOperateOn` solo se sobrescribe donde hace falta (`Administrator`); en los demas casos, incluido `Supervisor`, se queda con el comportamiento por defecto de `User` (solo opera sobre si mismo).

## Product y Warehouse usan un campo type en vez de subclases

Tanto `Product` como `Warehouse` tienen un campo de tipo (`ProductType`, `WarehouseType`) en vez de tener una subclase por cada variante, porque casi todos sus atributos son iguales sin importar el tipo — lo unico que cambia es un poco de comportamiento (`requiresPhysicalShipping()` en `Product`) o quien es el dueño (en `Warehouse`).

## Por que Seller guarda objetos y el resto guarda solo el id

`Seller` es el unico caso del dominio donde se guardan objetos completos (`Warehouse`, `Product`) en vez de solo el id. En todos los demas casos se usa el id (`Product.sellerId`, `Inventory.productId`/`warehouseId`, `CartItem.productId`, `OrderItem.productId`, `Shipment.orderId`/`logisticsOperatorId`, `Return.orderId`, `Refund.returnId`). `Return.refund` es la unica excepcion parecida a `Seller`: se guarda directo porque lo crea el propio `Return` y no existe sin el.

## Que clases son inmutables

* Las clases que representan un hecho que ya paso (`OrderItem`, `Invoice`, `Variant`) no cambian despues de creadas.
* Las clases que si tienen cambios de estado reales (`Product`, el nombre de `Warehouse`, `Inventory`, `ShoppingCart`, `CartItem`, `Order`, `Shipment`, `Return`, `Refund`) tienen metodos propios para cambiar sus datos, en vez de setters publicos, para que cada cambio pase por la validacion de la clase.
