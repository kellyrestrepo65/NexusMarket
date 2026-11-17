# Puertos de Salida

## Introduccion

Los puertos de salida son interfaces que pertenecen al dominio (`domain/ports/out`). Los servicios dependen solo de ellos; los adaptadores de persistencia (`application/adapters/out/persistence`) los implementan. Reciben y devuelven modelos de dominio, nunca entidades ni documentos.

---

# Puertos e implementaciones

| Puerto | Metodos | Adaptador | Almacen |
|---|---|---|---|
| UserRepositoryPort | `save`, `findById`, `findAll`, `existsByEmail`, `existsByIdentityDocument`, `existsByRole` | UserMysqlAdapter | MySQL `users`, `buyer_additional_addresses` |
| ProductRepositoryPort | `save`, `findById`, `findBySellerId`, `findByStatus` | ProductMongoAdapter | MongoDB `products` |
| WarehouseRepositoryPort | `save`, `findById`, `findByOwnerId`, `findAll` | WarehouseMysqlAdapter | MySQL `warehouses` |
| InventoryRepositoryPort | `save`, `findById`, `findByProductIdAndWarehouseId`, `findByProductId`, `findAll` | InventoryMysqlAdapter | MySQL `inventories` |
| ShoppingCartRepositoryPort | `save`, `findByBuyerId` | ShoppingCartMysqlAdapter | MySQL `shopping_carts`, `cart_items` |
| OrderRepositoryPort | `save`, `findById`, `findByBuyerId`, `findAll` | OrderMysqlAdapter | MySQL `orders`, `order_items` |
| InvoiceRepositoryPort | `save`, `findByOrderId`, `findAll` | InvoiceMysqlAdapter | MySQL `invoices` |
| ShipmentRepositoryPort | `save`, `findByOrderId`, `findAll` | ShipmentMysqlAdapter | MySQL `shipments` |
| ReturnRepositoryPort | `save`, `findById`, `findByOrderId`, `findAll` | ReturnMysqlAdapter | MySQL `returns`, `refunds` |

Los adaptadores MySQL usan `mysql/entities` (entidades JPA) y `mysql/repositories` (Spring Data JPA). El adaptador Mongo usa `mongodb/documents/ProductDocument` y `mongodb/repositories/ProductMongoRepository`.

---

# Reglas

- Los metodos `find*` devuelven `Optional` o `List`; decidir que un valor ausente es un error (`EntityNotFoundException`) es tarea del servicio.
- La conversion entre modelo y entidad vive en la propia entidad (`from` / `toDomain`), sin clases mapper. Al reconstruir se usan los constructores y metodos de estado del dominio, asi se vuelven a validar las invariantes.
- El inventario tiene columna `version` (bloqueo optimista): dos compras concurrentes no pueden reservar el mismo stock.
- La unicidad del documento y del correo se comprueba con `existsBy*` antes de guardar (`UniqueUserDataValidator`).
