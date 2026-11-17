package com.nexusmarket.domain.services.warehouse;

import com.nexusmarket.domain.models.user.Administrator;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.InvalidArgumentException;
import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.models.user.Seller;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.enums.WarehouseType;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.CreateWarehouseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dominio 4: el Administrador gestiona las bodegas, ya sean del Marketplace o de
 * un vendedor.
 */
@Service
public class CreateWarehouseService implements CreateWarehouseUseCase {

    private final AuthorizationService authorizationService;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public CreateWarehouseService(AuthorizationService authorizationService,
                                  WarehouseRepositoryPort warehouseRepositoryPort,
                                  UserRepositoryPort userRepositoryPort) {
        this.authorizationService = authorizationService;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Transactional
    @Override
    public Warehouse execute(EntityId actorId, String name, WarehouseType type, EntityId sellerId) {
        authorizationService.requireRole(actorId, Role.ADMINISTRATOR);
        if (type == WarehouseType.MARKETPLACE) {
            return warehouseRepositoryPort.save(Warehouse.marketplaceWarehouse(EntityId.next(), name));
        }
        if (sellerId == null) {
            throw new InvalidArgumentException("A seller warehouse requires the seller id");
        }
        User owner = userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
        if (!(owner instanceof Seller seller)) {
            throw new InvalidArgumentException("The user " + sellerId + " is not a seller");
        }
        Warehouse warehouse = Warehouse.sellerWarehouse(EntityId.next(), name, sellerId);
        seller.addWarehouse(warehouse);
        return warehouseRepositoryPort.save(warehouse);
    }
}
