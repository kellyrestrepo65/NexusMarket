package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * La columna version da bloqueo optimista: dos compras concurrentes que reservan
 * el mismo inventario no pueden tener exito ambas, asi que nunca se vende mas
 * stock del que hay.
 */
@Entity
@Table(name = "inventories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"productId", "warehouseId"}))
public class InventoryEntity {

    @Id
    private Integer id;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer warehouseId;

    private int availableQuantity;
    private int reservedQuantity;
    private int damagedQuantity;

    @Version
    private Long version;

    protected InventoryEntity() {
    }

    public static InventoryEntity create(Inventory inventory) {
        InventoryEntity entity = new InventoryEntity();
        entity.id = inventory.getId().value();
        entity.productId = inventory.getProductId().value();
        entity.warehouseId = inventory.getWarehouseId().value();
        entity.copyQuantities(inventory);
        return entity;
    }

    public void copyQuantities(Inventory inventory) {
        this.availableQuantity = inventory.getAvailableQuantity();
        this.reservedQuantity = inventory.getReservedQuantity();
        this.damagedQuantity = inventory.getDamagedQuantity();
    }

    public Inventory toDomain() {
        return Inventory.restore(EntityId.of(id), EntityId.of(productId), EntityId.of(warehouseId),
                availableQuantity, reservedQuantity, damagedQuantity);
    }

    public Integer getId() {
        return id;
    }
}
