# Value Objects del Dominio

## Introduccion

Ademas de las entidades (`User`, `Product`, `Order`, etc.), el dominio tiene un grupo de clases mas chicas que representan valores en vez de "cosas con identidad propia": un id, una direccion, o un estado dentro de una lista fija de opciones. Estas clases no tienen su propio repositorio ni se buscan por separado — siempre viven como un atributo de alguna entidad.

Hay dos tipos:

* `EntityId` y `Address`, que son clases propias.
* Los enums de estado y tipo (`Role`, `UserStatus`, `CommercialStatus`, `WarehouseType`, `ProductType`, `ProductStatus`, `ShipmentStatus`, `ReturnStatus`, `RefundStatus`, `OrderStatus`), que son enums normales de Java.

---

## EntityId

El id de cualquier entidad del dominio. En vez de usar `java.util.UUID` o un `long`/`String` suelto, todo el modelo usa esta clase para identificar usuarios, productos, ordenes, etc.

Por dentro, `EntityId` envuelve un numero de 6 digitos (entre `100000` y `999999`), generado con un contador (`AtomicInteger`) que empieza en `100000` y se incrementa cada vez que se pide un id nuevo. Se eligio un numero corto de 6 digitos, en vez de un UUID largo, para que sea mas facil de leer y de escribir a mano mientras se prueba el codigo.

### Atributos

| Atributo | Tipo | Por que existe |
| --- | --- | --- |
| value | int | El numero envuelto, siempre entre 100000 y 999999. |

### Como se crea

* `new EntityId(int value)` — valida que el numero este en el rango permitido; si no, lanza `InvalidArgumentException`.
* `EntityId.next()` — genera el siguiente id de la secuencia interna.
* `EntityId.of(int value)` — igual que el constructor.
* `EntityId.of(String value)` — convierte un texto a `EntityId`, validando que no este vacio y que sea un numero valido.

### Por que existe esta clase y no usar un numero o String suelto

* Evita que se pase por error un numero cualquiera (por ejemplo una cantidad o un precio) donde se espera un id.
* La validacion del rango queda en un solo lugar, en vez de repetirse cada vez que se recibe un id.
* `equals`, `hashCode` y `toString` estan definidos en base a `value`, asi que dos `EntityId` con el mismo numero se consideran iguales.

---

## Address

Una direccion postal. La usa `Buyer` como direccion principal (`mainAddress`) y en la lista de direcciones adicionales.

### Atributos

| Atributo | Tipo | Por que existe |
| --- | --- | --- |
| addressLine1 | String | La calle. Obligatoria. |
| city | String | La ciudad. Obligatoria. |
| stateOrProvince | String | El estado o provincia. Opcional. |
| country | String | El pais. Obligatorio. |
| postalCode | String | El codigo postal. Opcional. |

Es una clase inmutable: todos los campos son `final` y se fijan solo en el constructor, no hay setters. Dos direcciones con los mismos cinco valores se consideran iguales (no tienen un id propio que las distinga).

---

## Los enums simples

Los siguientes conceptos son enums de Java comunes, sin ningun comportamiento extra — solo representan una lista fija de valores posibles.

### Role

El rol de un usuario, fijado al crearlo y sin poder cambiar despues: `BUYER`, `SELLER`, `LOGISTICS_OPERATOR`, `ADMINISTRATOR`, `SUPERVISOR`.

### UserStatus

El estado general de la cuenta de cualquier usuario: `ACTIVE`, `BLOCKED`, `INACTIVE`.

### CommercialStatus

La situacion comercial de un `Buyer` (independiente de `UserStatus`): `ENABLED`, `RESTRICTED`. Para que un comprador pueda comprar hacen falta las dos cosas a la vez: estar `ACTIVE` y estar `ENABLED` (ver `Buyer.canPurchase()`).

### WarehouseType

Si una bodega es del marketplace o de un vendedor: `MARKETPLACE`, `SELLER`. Se usa este campo en vez de dos clases distintas de bodega porque lo unico que cambia entre las dos es quien es el dueño.

### ProductType

Si un producto es fisico o digital: `PHYSICAL`, `DIGITAL`. Igual que con `WarehouseType`, se uso un campo en vez de dos clases de producto, porque la unica diferencia real de comportamiento es si el producto necesita envio fisico.

### ProductStatus

El estado de publicacion de un producto: `PUBLISHED`, `SUSPENDED`, `DISCONTINUED`.

```text
PUBLISHED <--> SUSPENDED
     │              │
     └──> DISCONTINUED <──┘
```

Un producto puede ir de `PUBLISHED` a `SUSPENDED` y de vuelta las veces que haga falta, pero una vez que llega a `DISCONTINUED` ya no puede volver a ninguno de los otros dos.

### ShipmentStatus

El progreso de un envio: `IN_PREPARATION`, `IN_TRANSIT`, `DELIVERED`. Siempre en ese orden, sin poder saltarse pasos ni retroceder.

### ReturnStatus

El estado de una devolucion: `REQUESTED`, `APPROVED`, `REJECTED`. Cuando pasa a `APPROVED` se genera ademas un `Refund` (ver `Domain Model.md`).

### RefundStatus

El estado de un reembolso: `PENDING`, `PROCESSED`, `REJECTED`.

---

## OrderStatus, un poco distinto a los demas

`OrderStatus` tambien es un enum, pero a diferencia de los otros nueve, no es solo una lista de valores: cada valor sabe por si mismo a cual de los otros valores puede pasar. Esto se hace poniendole a cada constante su propio cuerpo con el metodo `canAdvanceTo` sobrescrito:

```java
public enum OrderStatus {
    CART            { public boolean canAdvanceTo(OrderStatus next) { return next == PENDING_PAYMENT; } },
    PENDING_PAYMENT { public boolean canAdvanceTo(OrderStatus next) { return next == PAID; } },
    PAID            { public boolean canAdvanceTo(OrderStatus next) { return next == DISPATCHED; } },
    DISPATCHED      { public boolean canAdvanceTo(OrderStatus next) { return next == DELIVERED; } },
    DELIVERED       { public boolean canAdvanceTo(OrderStatus next) { return false; } };

    public abstract boolean canAdvanceTo(OrderStatus next);
    public boolean isFinal() { return this == DELIVERED; }
}
```

Se hizo asi (en vez de, por ejemplo, un metodo con un `switch` en `Order`) para que cada estado tenga su propia regla al lado de su propio nombre, y para no tener que acordarse de actualizar un `switch` en otro archivo cada vez que se agregue o se cambie un estado.

### El recorrido de una orden

```text
CART ──> PENDING_PAYMENT ──> PAID ──> DISPATCHED ──> DELIVERED
```

* `CART` — la orden todavia no se confirmo, solo puede pasar a `PENDING_PAYMENT`.
* `PENDING_PAYMENT` — esperando que se confirme el pago, solo puede pasar a `PAID`.
* `PAID` — el pago ya se confirmo, solo puede pasar a `DISPATCHED`.
* `DISPATCHED` — el pedido ya salio para su entrega, solo puede pasar a `DELIVERED`.
* `DELIVERED` — estado final. `canAdvanceTo` siempre devuelve `false` aca, y es el unico estado donde `isFinal()` devuelve `true`.

`Order.advanceTo(nextStatus)` simplemente le pregunta a su estado actual (`status.canAdvanceTo(nextStatus)`) si el cambio es valido, y lanza `InvalidStateTransitionException` si no lo es. Esto evita, por ejemplo, que una orden pase directo de `CART` a `PAID` sin pasar por `PENDING_PAYMENT`, o que retroceda a un estado anterior.

Ademas, `Order.addItem(...)` y `Order.removeItem(...)` revisan `status.isFinal()` antes de dejar tocar los items — una orden `DELIVERED` no se puede modificar mas.

---

## Por que estos enums no tienen mas estructura

Estos enums de estado/tipo no tienen mas que sus constantes (sin codigo, nombre ni descripcion aparte) porque no hace falta: en ningun lado del proyecto se necesita mostrar un nombre distinto al de la propia constante, asi que agregarles mas campos solo hubiera sido codigo de mas sin usarse.

---

## Resumen de que value object usa cada entidad

```text
User.id, Product.id, Order.id, ... : EntityId  (todas las entidades)
User.role                          : Role
User.status                        : UserStatus
Buyer.mainAddress                  : Address
Buyer.commercialStatus             : CommercialStatus
Product.type                       : ProductType
Product.status                     : ProductStatus
Warehouse.type                     : WarehouseType
Order.status                       : OrderStatus
Shipment.status                    : ShipmentStatus
Return.status                      : ReturnStatus
Refund.status                      : RefundStatus
```

Usar estas clases y enums en vez de un `String` o un numero suelto hace mas dificil pasar un valor invalido por error, y deja mas claro, con solo mirar el tipo de un atributo, que valores puede tener.
