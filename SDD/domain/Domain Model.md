# Modelo de Dominio

## Introduccion

El modelo de dominio representa las entidades centrales del negocio de NexusMarket. Vive en `domain/models`. Las entidades encapsulan datos y las reglas que los mantienen consistentes; el estado solo cambia mediante metodos que validan la transicion.

---

# Jerarquia de clases del dominio

```text
User (abstracta)
├── Buyer
├── Seller
├── Administrator
├── LogisticsOperator
└── Supervisor

Product ── Variant (1..*)
Warehouse ── Inventory (por producto y bodega)
ShoppingCart ── CartItem (1..*)
Order ── OrderItem (1..*)
  ├── Invoice   (1 por orden pagada)
  ├── Shipment  (1 por orden fisica pagada)
  └── Return ── Refund (0..1)
AdministrativeReport (modelo de lectura)
```

---

# Usuarios (`models/user`)

## User (abstracta)

| Atributo | Tipo | Regla |
|---|---|---|
| id | EntityId | obligatorio |
| identityDocument | String | obligatorio, unico |
| fullName | String | obligatorio |
| email | String | obligatorio, valido, unico |
| role | Role | exactamente uno por usuario (RG-02) |
| status | UserStatus | ACTIVE / BLOCKED / INACTIVE; solo los activos operan |

## Buyer (comprador)

- `mainAddress: Address` (obligatoria) y `additionalAddresses: List<Address>`.
- `commercialStatus: CommercialStatus` (ENABLED / RESTRICTED). Un comprador restringido no puede comprar.
- Se autorregistra (la unica operacion publica).

## Seller (vendedor)

- Lo registra un Administrador junto con su primera bodega.
- Es dueño de `warehouses` (tipo SELLER) y de `products`.

## Administrator, LogisticsOperator, Supervisor

No tienen atributos extra; su comportamiento lo define la matriz de responsabilidades. El Supervisor solo consulta.

---

# Catalogo (`models/catalog`)

## Product (MongoDB)

| Atributo | Tipo | Regla |
|---|---|---|
| id | EntityId | |
| sellerId | EntityId | dueño |
| name | String | obligatorio |
| price | BigDecimal | ≥ 0 |
| type | ProductType | PHYSICAL / DIGITAL |
| status | ProductStatus | PUBLISHED / SUSPENDED / DISCONTINUED |
| variants | List<Variant> | pares atributo–valor |

Solo los productos PUBLISHED se pueden agregar al carrito. DISCONTINUED es final.

---

# Bodegas e inventario (`models/inventory`)

## Warehouse

`id`, `name`, `type: WarehouseType` (MARKETPLACE o SELLER), `ownerId` (vendedor, solo en bodegas SELLER).

## Inventory

Stock de un producto en una bodega: `availableQuantity`, `reservedQuantity`, `damagedQuantity`.

| Movimiento (especificacion) | Metodo | Regla |
|---|---|---|
| Ingreso | `receiveStock(q)` | q > 0 |
| Reserva | `reserve(q)` | q ≤ disponible − reservado; las unidades dañadas nunca se reservan |
| Salida por venta | `confirmDispatch(q)` | q ≤ reservado |
| Ajuste | `adjust(delta)` | nunca por debajo de 0 ni de lo reservado |
| Devolucion | `registerReturn(q)` | q > 0 |
| (Daño) | `markAsDamaged(q)` | pasa stock libre a dañado |

---

# Compra

## ShoppingCart / CartItem (`models/cart`)

Un carrito por comprador. Los items guardan `productId` y `quantity`; el precio se fija en el checkout.

## Order / OrderItem (`models/order`)

- Los items copian `productId`, `quantity` y `unitPrice` en el checkout; `total` = Σ cantidad × precio unitario.
- Ciclo de vida (`OrderStatus.canAdvanceTo`): `CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED`. No se salta ni se retrocede ningun estado.
- Una orden DELIVERED es final y no se puede modificar (`OrderFinalizedException`).
- Una orden con solo productos DIGITAL pasa de PAID directamente a DELIVERED y no tiene envio.

## Invoice (factura)

Se crea al pagar la orden: `orderId`, `total`, `issueDate`.

## Shipment (envio)

Se crea al pagar una orden con productos fisicos: `IN_PREPARATION → IN_TRANSIT (despacho) → DELIVERED`, con el `logisticsOperatorId` que despacho.

## Return / Refund (devolucion / reembolso)

- Solo para ordenes DELIVERED, una por orden: `REQUESTED → APPROVED | REJECTED`.
- Al aprobarla se crea un `Refund` (PENDING) por el total de la orden y las unidades vuelven al inventario.
- Reembolso: `PENDING → PROCESSED | REJECTED`, lo gestiona el Administrador.

---

# AdministrativeReport (`models/report`)

Modelo de lectura que consolida conteos y montos (usuarios por rol, ordenes por estado, total vendido, facturas, envios, devoluciones y reembolsos) para el Administrador y el Supervisor.
