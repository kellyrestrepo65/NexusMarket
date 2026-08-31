package com.nexusmarket.domain.model.inventory;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.WarehouseType;

import java.util.Objects;

/**
 * Una bodega. Puede ser del marketplace (ownerId es null) o de un
 * vendedor especifico (ownerId es obligatorio). Se usa una sola clase
 * con el campo type en vez de dos clases separadas porque el
 * comportamiento es el mismo, solo cambia el dueño.
 */
public class Warehouse {

    private final EntityId id;
    private String name;
    private final WarehouseType type;
    private final EntityId ownerId;

    private Warehouse(EntityId id, String name, WarehouseType type, EntityId ownerId) {
        if (id == null) {
            throw new InvalidArgumentException("The warehouse id is required");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidArgumentException("The warehouse name cannot be empty");
        }
        this.id = id;
        this.name = name;
        this.type = type;
        this.ownerId = ownerId;
    }

    public static Warehouse marketplaceWarehouse(EntityId id, String name) {
        return new Warehouse(id, name, WarehouseType.MARKETPLACE, null);
    }

    public static Warehouse sellerWarehouse(EntityId id, String name, EntityId ownerSellerId) {
        if (ownerSellerId == null) {
            throw new InvalidArgumentException("A seller warehouse requires an owner");
        }
        return new Warehouse(id, name, WarehouseType.SELLER, ownerSellerId);
    }

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new InvalidArgumentException("The warehouse name cannot be empty");
        }
        this.name = newName;
    }

    public EntityId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public WarehouseType getType() {
        return type;
    }

    public EntityId getOwnerId() {
        return ownerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Warehouse)) return false;
        Warehouse warehouse = (Warehouse) o;
        return Objects.equals(id, warehouse.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
