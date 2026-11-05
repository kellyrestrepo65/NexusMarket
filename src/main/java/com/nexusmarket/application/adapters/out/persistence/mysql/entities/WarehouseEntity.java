package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.WarehouseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "warehouses")
public class WarehouseEntity {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseType type;

    private Integer ownerId;

    protected WarehouseEntity() {
    }

    public static WarehouseEntity from(Warehouse warehouse) {
        WarehouseEntity entity = new WarehouseEntity();
        entity.id = warehouse.getId().value();
        entity.name = warehouse.getName();
        entity.type = warehouse.getType();
        entity.ownerId = warehouse.getOwnerId() == null ? null : warehouse.getOwnerId().value();
        return entity;
    }

    public Warehouse toDomain() {
        if (type == WarehouseType.MARKETPLACE) {
            return Warehouse.marketplaceWarehouse(EntityId.of(id), name);
        }
        return Warehouse.sellerWarehouse(EntityId.of(id), name, EntityId.of(ownerId));
    }

    public Integer getId() {
        return id;
    }
}
