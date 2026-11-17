package com.nexusmarket.application.adapters.out.persistence.mysql.adapters;

import com.nexusmarket.domain.models.order.Shipment;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.ShipmentEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.ShipmentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ShipmentMysqlAdapter implements ShipmentRepositoryPort {

    private final ShipmentJpaRepository shipmentJpaRepository;

    public ShipmentMysqlAdapter(ShipmentJpaRepository shipmentJpaRepository) {
        this.shipmentJpaRepository = shipmentJpaRepository;
    }

    @Override
    public Shipment save(Shipment shipment) {
        shipmentJpaRepository.save(ShipmentEntity.from(shipment));
        return shipment;
    }

    @Override
    public Optional<Shipment> findByOrderId(EntityId orderId) {
        return shipmentJpaRepository.findByOrderId(orderId.value()).map(ShipmentEntity::toDomain);
    }

    @Override
    public List<Shipment> findAll() {
        return shipmentJpaRepository.findAll().stream().map(ShipmentEntity::toDomain).toList();
    }
}
