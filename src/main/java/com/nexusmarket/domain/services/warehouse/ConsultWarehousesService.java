package com.nexusmarket.domain.services.warehouse;

import com.nexusmarket.domain.models.user.Administrator;
import com.nexusmarket.domain.models.user.Supervisor;

import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultWarehousesUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Un vendedor solo ve sus propias bodegas; el Administrador, el Operador
 * Logistico y el Supervisor las ven todas.
 */
@Service
public class ConsultWarehousesService implements ConsultWarehousesUseCase {

    private final AuthorizationService authorizationService;
    private final WarehouseRepositoryPort warehouseRepositoryPort;

    public ConsultWarehousesService(AuthorizationService authorizationService,
                                    WarehouseRepositoryPort warehouseRepositoryPort) {
        this.authorizationService = authorizationService;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
    }

    @Override
    public List<Warehouse> execute(EntityId actorId) {
        User actor = authorizationService.requireRole(actorId,
                Role.ADMINISTRATOR, Role.SELLER, Role.LOGISTICS_OPERATOR, Role.SUPERVISOR);
        if (actor.getRole() == Role.SELLER) {
            return warehouseRepositoryPort.findByOwnerId(actor.getId());
        }
        return warehouseRepositoryPort.findAll();
    }
}
