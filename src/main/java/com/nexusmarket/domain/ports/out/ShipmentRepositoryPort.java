package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.order.Shipment;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepositoryPort {

    Shipment save(Shipment shipment);

    Optional<Shipment> findByOrderId(EntityId orderId);

    List<Shipment> findAll();
}
