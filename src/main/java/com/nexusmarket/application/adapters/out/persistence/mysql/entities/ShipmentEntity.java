package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.models.order.Shipment;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ShipmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipments")
public class ShipmentEntity {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer orderId;

    private Integer logisticsOperatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    protected ShipmentEntity() {
    }

    public static ShipmentEntity from(Shipment shipment) {
        ShipmentEntity entity = new ShipmentEntity();
        entity.id = shipment.getId().value();
        entity.orderId = shipment.getOrderId().value();
        entity.logisticsOperatorId = shipment.getLogisticsOperatorId() == null
                ? null : shipment.getLogisticsOperatorId().value();
        entity.status = shipment.getStatus();
        return entity;
    }

    public Shipment toDomain() {
        return Shipment.restore(EntityId.of(id), EntityId.of(orderId),
                logisticsOperatorId == null ? null : EntityId.of(logisticsOperatorId), status);
    }
}
