# Objetos de Valor y Enumeraciones del Dominio

## Introduccion

Los objetos de valor son inmutables, no tienen identidad y se comparan por su valor. Viven en `domain/valueobjects`; las enumeraciones de roles y estados viven en `domain/enums`.

---

# Objetos de valor (`domain/valueobjects`)

## EntityId

Envuelve el identificador numerico de toda entidad (`int value`). Rechaza valores nulos o invalidos, asi los servicios nunca reciben primitivos sueltos como identificadores. `EntityId.of(int)`; `toString()` da el id textual que usa la API. `EntityId.next()` genera el siguiente id de una secuencia que, al arrancar, continua despues del id mas alto ya guardado.

## Address

`addressLine1` (obligatorio), `city` (obligatorio), `stateOrProvince`, `country` (obligatorio), `postalCode`. Se usa para la direccion principal y las adicionales del comprador.

---

# Enumeraciones (`domain/enums`)

| Enum | Valores | Notas |
|---|---|---|
| Role | BUYER, SELLER, LOGISTICS_OPERATOR, ADMINISTRATOR, SUPERVISOR | uno por usuario (RG-02) |
| UserStatus | ACTIVE, BLOCKED, INACTIVE | solo los usuarios ACTIVE operan |
| CommercialStatus | ENABLED, RESTRICTED | estado comercial del comprador |
| ProductType | PHYSICAL, DIGITAL | los productos digitales no se envian |
| ProductStatus | PUBLISHED, SUSPENDED, DISCONTINUED | DISCONTINUED es final |
| WarehouseType | MARKETPLACE, SELLER | |
| OrderStatus | CART, PENDING_PAYMENT, PAID, DISPATCHED, DELIVERED | `canAdvanceTo(next)`, `isFinal()` |
| ShipmentStatus | IN_PREPARATION, IN_TRANSIT, DELIVERED | |
| ReturnStatus | REQUESTED, APPROVED, REJECTED | |
| RefundStatus | PENDING, PROCESSED, REJECTED | |
