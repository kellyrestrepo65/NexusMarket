package com.nexusmarket.domain.model.order;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.exception.InvalidStateTransitionException;
import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.ShipmentStatus;

import java.util.Objects;

/**
 * El envio de una orden. Solo existe si la orden tiene al menos un
 * producto fisico (ver Product.requiresPhysicalShipping()); una orden
 * hecha solo de productos digitales nunca tiene un Shipment asociado.
 */
public class Shipment {

    private final EntityId id;
    private final EntityId orderId;
    private EntityId logisticsOperatorId;
    private ShipmentStatus status;

    public Shipment(EntityId id, EntityId orderId) {
        if (id == null) {
            throw new InvalidArgumentException("The shipment id is required");
        }
        if (orderId == null) {
            throw new InvalidArgumentException("The shipment must be associated with an order");
        }
        this.id = id;
        this.orderId = orderId;
        this.status = ShipmentStatus.IN_PREPARATION;
    }

    public void assignOperator(EntityId logisticsOperatorId) {
        if (logisticsOperatorId == null) {
            throw new InvalidArgumentException("The logistics operator cannot be null");
        }
        this.logisticsOperatorId = logisticsOperatorId;
    }

    public void markInTransit() {
        if (this.status != ShipmentStatus.IN_PREPARATION) {
            throw new InvalidStateTransitionException("Only a shipment in preparation can move to in transit");
        }
        this.status = ShipmentStatus.IN_TRANSIT;
    }

    public void markDelivered() {
        if (this.status != ShipmentStatus.IN_TRANSIT) {
            throw new InvalidStateTransitionException("Only a shipment in transit can be marked as delivered");
        }
        this.status = ShipmentStatus.DELIVERED;
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getOrderId() {
        return orderId;
    }

    public EntityId getLogisticsOperatorId() {
        return logisticsOperatorId;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment)) return false;
        Shipment shipment = (Shipment) o;
        return Objects.equals(id, shipment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
